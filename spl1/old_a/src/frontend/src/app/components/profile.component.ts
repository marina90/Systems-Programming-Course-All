import {Component, Directive, Input, OnInit} from '@angular/core'
import {Hashtag, HashtagService} from '../services/hashtag.service';
import {default_user, ProfileService, User} from '../services/profile.service';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'profile',
  templateUrl: './html/profile.component.html',
  styleUrls: ['./css/profile.component.css'],
})

export class ProfileComponent implements OnInit {
  display_name: string;
  active_user_display_name: string;
  activeButtonId: number = 1;
  following: string[] = [];
  followers: string[] = [];
  isFollowing: boolean = false;
  profile: User = default_user;

  constructor(
    private activatedRoute: ActivatedRoute,
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router) {}

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((params: Params) => {
      this.display_name = params['display_name'];
      this.updateFields(this.display_name);
    });
  }

  updateFields(displayName: string): void {
    this.validateRealUser(displayName).then(res => {
      if (res) {
        this.active_user_display_name = this.authService.getDisplayName();
        this.updateFollowing();
        this.updateFollowers();
      } else {
        this.router.navigate(["page-not-found"]);
      }
    })
  }

  validateRealUser(displayName: string): Promise<boolean> {
    return this.profileService.getProfile(displayName).then(res=> {
      if (res.message == "Success") {
        this.profile = res.user;
        return true;
      } else {
        return false;
      }
    });
  }

  updateFollowing(): void {
    this.following = [];
    this.profileService.getFollowing(this.display_name)
      .then(res => res.user.user_follow_list.map(x => this.following.push(x.display_name)));
  }

  updateFollowers(): void {
    this.followers = [];
    this.profileService.getFollowers(this.display_name)
      .then(res => this.followers = res.docs.map(x => x.display_name))
      .then(res => this.isFollowing = (undefined != this.followers.find(x => this.active_user_display_name == x)));
  }

  follow(): void {
    if (!this.isFollowing) {
      this.profileService.follow(this.active_user_display_name, this.display_name)
        .then(res => this.updateFollowers());
    } else {
      this.profileService.unfollow(this.active_user_display_name, this.display_name)
        .then(res => this.updateFollowers());
    }
  }
}
