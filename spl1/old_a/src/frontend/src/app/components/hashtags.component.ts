import {Component, OnInit} from '@angular/core'
import {Hashtag, HashtagService} from '../services/hashtag.service';

@Component({
  selector: 'hashtags',
  templateUrl: './html/hashtags.component.html',
  styleUrls: []
})

export class HashtagComponent implements OnInit {
  hashtags: Hashtag[] = [];

  constructor(private hashtagService: HashtagService) {}

  ngOnInit(): void {
    this.refreshHashtags();
  }

  private refreshHashtags(): void {
    this.hashtagService.getPopularHashtags().then(hashtags => this.hashtags = hashtags);
  }

}
