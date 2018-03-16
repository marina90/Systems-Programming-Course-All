import {Component, Input, OnInit} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Post, PostService} from '../services/post.service';
import {AuthService} from '../services/auth.service';
import {selectValueAccessor} from '@angular/forms/src/directives/shared';

@Component({
  selector: 'posts',
  templateUrl: './html/posts.component.html',
  styleUrls: ['./css/posts.component.css'],
})

export class PostsComponent implements OnInit {
  posts: Post[];
  postsLiked: boolean[] = [];
  active_user: string;

  constructor(
    private authService: AuthService,
    private postService: PostService,
    private activatedRoute: ActivatedRoute,
    private router: Router) {}

  ngOnInit(): void {
    this.active_user = this.authService.getDisplayName();

    this.activatedRoute.params.subscribe((params: Params) => {
      let display_name = params['display_name'];
      let search = params['search'];
      let type = params['type'];
      let postsPromise : Promise<Post[]>;

      if (display_name) {
        postsPromise = this.postService.getPostsByUser(display_name);
      } else if (search) {
        postsPromise = this.postService.getPostsFromSearch(search);
      } else if (type == "news") {
        postsPromise = this.postService.getPosts();
      } else if (type == "feed") {
        postsPromise = this.postService.getPostsForUser();
      } else {
        this.router.navigate(["welcome"]);
      }

      postsPromise
        .then(posts => this.posts = posts)
        .then(res => this.posts.forEach((post, index) => this.updatePostLiked(post,index)));
    });




  }

  like(post: Post, index: number): void {
    let votePromise;

    if (!this.postsLiked[index]) {
      votePromise = this.postService.votePost(post._id);
    } else {
      votePromise = this.postService.unvotePost(post._id)
    }

    votePromise.then(res => {
      post.voters = res.raw.voters;
      this.updatePostLiked(post, index);
    });

  }

  comment(post: Post, content: string): void {
    this.postService.addComment(post._id, content).then(x => {
      post.comments = x;
    });

  }

  private updatePostLiked(post: Post, index: number) {
    this.postsLiked[index] = (undefined != post.voters.find(x => (x.display_name == this.active_user)));
  }
}


