import {Component} from '@angular/core';
import {AuthService} from '../services/auth.service'

@Component({
  selector: 'login',
  templateUrl: './html/login.component.html',
  styleUrls: ['./css/login.component.css']
})

export class LoginComponent {
  errorMsg: string = "";
  constructor(
    private authService: AuthService) {}

  login(username: string, password: string): void {
    this.authService.login(username, password)
      .then(res => {
          // This is intentionally not done using a router so the frame will refresh.
          window.location.href = "/welcome";
      }).catch(err => {
        this.errorMsg = err;
    });

  }

}
