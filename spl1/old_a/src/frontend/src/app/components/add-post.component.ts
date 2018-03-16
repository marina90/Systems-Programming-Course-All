import {Component, OnInit} from '@angular/core'
import {AuthService} from '../services/auth.service';
import {Router} from '@angular/router';
import {Post, PostStep, PostService} from '../services/post.service';

@Component({
  selector: 'add-post',
  templateUrl: './html/add-post.component.html',
  styleUrls: ['./css/add-post.component.css'],
})

export class AddPostComponent implements OnInit {

  display_name: string;
  today: string;
  post: Post = new Post();
  step: PostStep = new PostStep();
  step_images : File[] = [];
  step_image : File;
  hashtag: string;
  coauthor: string;
  material: string;
  titleError: string;
  descriptionError: string;
  authorsError: string;

  constructor(
    private postService: PostService,
    private authService: AuthService,
    private router: Router) {
    if (!this.authService.isLoggedIn()) {
      router.navigate(['page-not-found']);
    }
    this.display_name = this.authService.getDisplayName();
    this.today = new Date().toJSON().slice(0,16);
  }

  ngOnInit(): void {

  }

  addHashtag(): void {
    if (this.hashtag) {
      this.post.hashtags.push(this.hashtag);
      this.hashtag = '';
    }
  }

  removeHashtag(hashtagIndex: number) : void {
    this.post.hashtags.splice(hashtagIndex, 1);
  }

  addCoauthor() : void {
    if (this.coauthor) {
      this.post.authors.push(this.coauthor);
      this.coauthor = '';
    }
  }

  removeCoauthor(coauthorIndex: number) : void {
    this.post.authors.splice(coauthorIndex, 1);
  }

  addStep() : void {
    this.post.steps.push(this.step);
    this.step_images.push(this.step_image);
    this.step = new PostStep();
  }

  removeStep(stepIndex: number) : void {
    this.post.steps.splice(stepIndex, 1);
    this.step_images.splice(stepIndex, 1);
  }

  addMaterial() : void {
    if (this.material) {
      this.step.materials.push(this.material);
      this.material = '';
    }
  }

  removeMaterial(materialIndex: number) : void {
    this.step.materials.splice(materialIndex, 1);
  }

  onChangeFile(event : Event) : void {
    this.step_image = (event.srcElement as HTMLInputElement).files.item(0);
  }

  submit_post(): void {
    this.titleError = undefined;
    this.descriptionError = undefined;
    this.authorsError = undefined;

    if ((this.post.authors.length == 0) || (this.post.authors[0] != this.display_name)) {
      this.post.authors.splice(0, 0, this.display_name); // add self as author
    }

    this.postService.addPost(this.post, this.step_images)
      .then(res => this.router.navigate(["posts","feed"]))
      .catch(err => {
        if (err.hasOwnProperty("title")) {
          this.titleError = err.title.msg;
        }
        if (err.hasOwnProperty("description")) {
          this.descriptionError = err.description.msg;
        }
        if (err.hasOwnProperty("error")) {
          this.authorsError = err.error;
        }
      });

  }
}
