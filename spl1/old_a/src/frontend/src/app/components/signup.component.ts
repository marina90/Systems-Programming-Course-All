import {Component} from '@angular/core';
import {AuthService} from '../services/auth.service'

@Component({
  selector: 'signup',
  templateUrl: './html/signup.component.html',
  styleUrls: ['./css/signup.component.css']
})

export class SignupComponent {
  displayNameError: string = null;
  emailError: string = null;
  passwordError: string = null;
  constructor(private authService: AuthService) {}

  signup(displayName: string, email: string, password: string, description: string): void {
    this.authService.register(displayName, email, password, description)
      .then(res => {
          window.location.href = "/welcome";
      }).catch(error => {
        if (error.hasOwnProperty("display_name")) {
          this.displayNameError = error.display_name.msg;
        } else {
          this.displayNameError = null;
        }
        if (error.hasOwnProperty("email")) {
          this.emailError = error.email.msg;
        } else {
          this.emailError = null;
        }
        if (error.hasOwnProperty("password")) {
          this.passwordError = error.password.msg;
        } else {
          this.passwordError = null;
        }
    });

  }

}
