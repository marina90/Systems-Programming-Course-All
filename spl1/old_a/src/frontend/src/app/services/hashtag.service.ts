import { Injectable } from '@angular/core';
import {Http, Headers} from '@angular/http';
import 'rxjs/add/operator/toPromise';

export class Hashtag {
  _id: String;
  name : String;
  counter : Number;
}

@Injectable()
export class HashtagService {
  private hashtagUrl = 'api/v1/hashtags';
  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http) {}

  getPopularHashtags(): Promise<Hashtag[]> {
    return this.http.get(this.hashtagUrl+"/popular")
      .toPromise()
      .then(response => response.json() as Hashtag[])
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    return Promise.reject(error.message || error);
  }
}
