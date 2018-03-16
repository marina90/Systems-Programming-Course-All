import { Injectable } from '@angular/core';
import {CookieService} from 'angular2-cookie/core';
import {Http, Headers, RequestOptionsArgs} from '@angular/http';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class AuthService {
  private tokenCookieName: string = 'token';
  private displayNameCookieName: string = 'display_name';
  private loginUrl = 'api/v1/login';
  private registerUrl = 'api/v1/register';

  constructor(private http: Http, private cookieService: CookieService) {}

  login(display_name: string, password: string): Promise<any> {
    return this.http.post(
      this.loginUrl,
      JSON.stringify({display_name: display_name, password: password}),
      this.getRequestOptionsArgs())
      .toPromise()
      .then(res => {
          this.cookieService.put(this.tokenCookieName, res.json().token);
          this.cookieService.put(this.displayNameCookieName, display_name);
      })
      .catch(this.handleLoginError);
  }

  register(display_name: string, email: string, password: string, description: string) : Promise<any> {
    return this.http.post(
      this.registerUrl,
      JSON.stringify({display_name: display_name, email: email, password: password, description: description}),
      this.getRequestOptionsArgs())
      .toPromise()
      .then(res => {
        this.cookieService.put(this.tokenCookieName, res.json().token);
        this.cookieService.put(this.displayNameCookieName, display_name);
      })
      .catch(this.handleRegisterError);
  }

  private handleLoginError(res: any): Promise<any> {
    return Promise.reject(res.json().error);
  }

  private handleRegisterError(res: any): Promise<any> {
    return Promise.reject(res.json());
  }

  getToken(): string {
    return this.cookieService.get(this.tokenCookieName);
  }

  isLoggedIn(): boolean {
    return this.getToken() != null;
  }

  getDisplayName(): string {
    return this.cookieService.get(this.displayNameCookieName);
  }

  getHeaders(): Headers {
    let headers = this.getHeadersWithoutContentType()
    headers.append('Content-Type', 'application/json');
    return headers;
  }

  getHeadersWithoutContentType(): Headers {
    if (this.isLoggedIn()) {
      return new Headers({
        'Authorization': 'JWT ' + this.getToken()
      });
    } else {
      return new Headers({});
    }
  }

  getRequestOptionsArgs(): RequestOptionsArgs {
    return {headers: this.getHeaders()};
  }

  logout(): void {
    this.cookieService.remove(this.tokenCookieName);
    this.cookieService.remove(this.displayNameCookieName);
  }
}
