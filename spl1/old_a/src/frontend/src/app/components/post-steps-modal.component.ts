import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {Post} from '../services/post.service';
import {UUID} from 'angular2-uuid';

@Component({
  selector: 'post-steps-modal',
  templateUrl: './html/post-steps-modal.component.html',
  styleUrls: ['./css/post-steps-modal.component.css']
})

export class PostStepsModalComponent {
  @Input() post: Post;
  uuid: string = UUID.UUID();

  constructor() {
  }
}

