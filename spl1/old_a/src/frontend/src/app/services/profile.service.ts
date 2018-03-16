import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import 'rxjs/add/operator/toPromise';
import {AuthService} from './auth.service';

export class User {
  display_name: string;
  description: string;
  avatar: string;
  user_follow_list: string[];
  hashtag_follow_list: string[];
  creation_date: Date;
  email: string;
}

export const default_user : User = {
  display_name: "",
  description: "",
  avatar: "",
  user_follow_list: [],
  hashtag_follow_list: [],
  creation_date: new Date(),
  email: ""
}

@Injectable()
export class ProfileService {
  private profilesUrl = 'api/v1/profile';

  constructor(private http: Http, private authService: AuthService) {}

  unfollow(displayName: string, toUnfollow: string) : Promise<any>{
    return this.http.delete(this.profilesUrl+"/"+displayName+"/follow/"+toUnfollow, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json())
      .catch(this.handleError);
  }

  follow(displayName: string, toFollow: string) : Promise<any>{
    return this.http.post(this.profilesUrl+"/"+displayName+"/follow/"+toFollow, "", this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json())
      .catch(this.handleError);
  }

  getFollowing(displayName: string): Promise<any> {
    return this.http.get(this.profilesUrl+"/"+displayName+"/following", this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json())
      .catch(this.handleError);
  }

  getFollowers(displayName: string): Promise<any> {
    return this.http.get(this.profilesUrl+"/"+displayName+"/followers", this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json())
      .catch(this.handleError);
  }

  getProfile(display_name: string) : Promise<any> {
    return this.http.get(this.profilesUrl+"/"+display_name, this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(response => response.json())
      .catch(this.handleError);
  }

  updateProfile(user: User, new_avatar: File) : Promise<any> {
    return this.http.put(this.profilesUrl+"/"+user.display_name, JSON.stringify(user), this.authService.getRequestOptionsArgs())
      .toPromise()
      .then(res => {
        if (new_avatar != undefined) {
          this.updateProfileAvatar(user.display_name, new_avatar)
        }})
      .catch(this.handleError);
  }

  private updateProfileAvatar(display_name: string, new_avatar: File) : Promise<any> {
    let formData = new FormData();
    formData.append('avatar', new_avatar);
    return this.http.put(this.profilesUrl+"/"+display_name+"/image", formData,
      { headers: this.authService.getHeadersWithoutContentType()})
      .toPromise()
      .catch(this.handleError);
  }

  private handleError(res: any): Promise<any> {
    return Promise.reject(res.json());
  }
}
