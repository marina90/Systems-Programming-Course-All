import {Component, OnInit} from '@angular/core'
import {default_user, ProfileService, User} from '../services/profile.service';
import {AuthService} from '../services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'edit-profile',
  templateUrl: './html/edit-profile.component.html',
  styleUrls: ['./css/edit-profile.component.css']
})

export class EditProfileComponent implements OnInit {
  display_name: string;
  profile: User = default_user;
  new_avatar : File;
  emailError: string;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router) {
    if (!this.authService.isLoggedIn()) {
      router.navigate(["page-not-found"]);
    }
    this.display_name = this.authService.getDisplayName();
  }

  ngOnInit(): void {
    this.profileService.getProfile(this.display_name).then(res => this.profile = res.user);
  }

  onChangeFile(event : Event) : void {
    this.new_avatar = (event.srcElement as HTMLInputElement).files.item(0);
  }

  save(): void {
    this.profileService.updateProfile(this.profile, this.new_avatar)
      .then(res => this.router.navigate(["profile/", this.display_name]))
      .catch(err => {
        this.emailError = err.email.msg;
      })
  }

  cancel(): void {
    this.router.navigate(["profile/", this.display_name]);
  }
}
