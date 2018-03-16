import { Injectable } from '@angular/core';
import {Http, Headers, RequestOptionsArgs} from '@angular/http';
import 'rxjs/add/operator/toPromise';
import {User} from 'app/services/profile.service';
import {Hashtag} from './hashtag.service';
import {AuthService} from './auth.service';

export class PostStep {
  title: string;
  cover: string;
  description: string;
  materials: string[];

  constructor() {
    this.title = '';
    this.cover = '';
    this.description = '';
    this.materials = [];
  }
}

export class Comment {
  body: string;
  date: Date;
  user: User;
}

export class Post {
  _id: number;
  authors: string[];
  title: string;
  description: string;
  cover : string;
  hashtags : string[];
  comments: Comment[];
  upload_date: Date;
  creation_date: Date;
  voters: User[];
  steps: PostStep[];

  constructor() {
    this._id = null;
    this.authors = [];
    this.title = '';
    this.description = '';
    this.cover = '';
    this.hashtags = [];
    this.comments = [];
    this.upload_date = new Date();
    this.creation_date = new Date();
    this.voters = [];
    this.steps = [];
  }
}

@Injectable()
export class PostService {
  private postsUrl = 'api/v1/posts';

  constructor(private http: Http, private authService: AuthService) {}

  getPosts(): Promise<Post[]> {
    return this.http.get(this.postsUrl, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json() as Post[])
      .catch(this.handleError);
  }

  getPostsByUser(display_name: string): Promise<Post[]> {
    return this.http.get(this.postsUrl+"/name/"+display_name, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json() as Post[])
      .catch(this.handleError);
  }

  getPostsWithHashtag(hashtag: string): Promise<Post[]> {
    return this.http.get(this.postsUrl+"/hashtag/"+hashtag, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json() as Post[])
      .catch(this.handleError);
  }

  getPostsForUser(): Promise<Post[]> {
    return this.http.get(this.postsUrl+"/stream", this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json() as Post[])
      .catch(this.handleError);
  }

  getPostsFromSearch(query: string): Promise<Post[]> {
    return this.http.post(this.postsUrl+"/search", {search: query}, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json() as Post[])
      .catch(this.handleError);
  }

  addComment(postId: number, comment: string): Promise<Comment[]> {
    return this.http.post(this.postsUrl+"/"+postId.toString(16)+"/comment", {comment: comment}, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(res => (res.json().raw as Post).comments)
      .catch(this.handleError);
  }

  votePost(postId: number): Promise<any> {
    return this.http.post(this.postsUrl+"/"+postId.toString(16)+"/vote", "", this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(res => res.json())
      .catch(this.handleError);
  }

  unvotePost(postId: number): Promise<any> {
    return this.http.delete(this.postsUrl+"/"+postId.toString(16)+"/vote", this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(res => res.json())
      .catch(this.handleError);
  }

  addPost(post: Post, postStepsFiles: File[]): Promise<any> {
    let postSteps = post.steps;
    post.steps = [];

    return this.http.post(this.postsUrl, JSON.stringify(post), this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(res => {
        let postId : number = res.json().post as number;
        this.addSubPosts(postId, postSteps, postStepsFiles, 0);
      })
      .catch(err => {
        post.steps = postSteps;
        return this.handleError(err);
      });
  }

  private addSubPosts(postId: number, postSteps: PostStep[], postStepsFiles: File[], postIndex: number) {
    if (postIndex >= postSteps.length) {
      return;
    }
    this.addSubPost(postId, postSteps[postIndex]).then(res => {
      if (postStepsFiles[postIndex] != undefined) {
        this.uploadSubPostCover(postId, postIndex, postStepsFiles[postIndex]);
      }}).then(res => {
      this.addSubPosts(postId, postSteps, postStepsFiles, postIndex + 1);
    })
  }

  private addSubPost(postId: number, postStep: PostStep) : Promise<any> {
    return this.http.post(this.postsUrl+"/"+postId.toString(16)+"/subpost", JSON.stringify(postStep),
      this.authService.getRequestOptionsArgs())
      .toPromise()
      .catch(this.handleError);
  }

  private uploadSubPostCover(postId: number, postStepIndex: number, postCoverFile: File) : Promise<any> {
    let formData = new FormData();
    formData.append('cover', postCoverFile);
    return this.http.post(this.postsUrl+"/"+postId.toString(16)+"/"+postStepIndex.toString(16)+"/cover", formData,
      { headers: this.authService.getHeadersWithoutContentType()})
      .toPromise()
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    return Promise.reject(error.json());
  }
}
