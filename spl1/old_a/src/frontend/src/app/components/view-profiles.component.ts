import {Component, Input} from '@angular/core';
import {AuthService} from '../services/auth.service'
import {User} from '../services/profile.service';

@Component({
  selector: 'view-profiles',
  templateUrl: './html/view-profiles.component.html',
  styleUrls: []
})

export class ViewProfilesComponent {
  @Input() profile_names: string[];
  constructor(private authService: AuthService) {}

}
