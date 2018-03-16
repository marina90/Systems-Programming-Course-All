import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {ProfileService, User, default_user} from '../services/profile.service';
import {AuthService} from 'app/services/auth.service';

@Component({
  selector: 'profile-card',
  templateUrl: './html/profile-card.component.html',
  styleUrls: ['./css/profile-card.component.css']
})

export class ProfileCardComponent implements OnInit {
  @Input() profile_name: string;
  profile: User = default_user;
  followers: string[] = [];
  active_user: string;
  isFollowing: boolean = false;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,) {}

  ngOnInit(): void {
    this.profileService.getProfile(this.profile_name).then(res => this.profile = res.user);
    this.active_user = this.authService.getDisplayName();
    this.setIsFollowing();
    this.setFollowersList();
  }

  setIsFollowing(): void {
    if (this.authService.isLoggedIn()) {
      this.profileService.getFollowing(this.active_user).then(
        res => this.isFollowing = (undefined != res.user.user_follow_list.find(x => (x.display_name == this.profile_name))));
    }
  }

  setFollowersList(): void {
    this.profileService.getFollowers(this.profile_name).then(res => this.followers = res.docs.map(x => x.display_name));
  }

  follow(): void {
    this.isFollowing = !this.isFollowing;
    if (this.isFollowing) {
      this.profileService.follow(this.active_user, this.profile_name);
    } else {
      this.profileService.unfollow(this.active_user, this.profile_name);
    }
  }
}

