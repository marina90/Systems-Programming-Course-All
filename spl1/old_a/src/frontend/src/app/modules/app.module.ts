import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {AppComponent} from '../components/app.component'
import {PostsComponent} from '../components/posts.component';
import {HashtagComponent} from '../components/hashtags.component';
import {AppRoutingModule} from './app-routing.module';
import {PostService} from '../services/post.service';
import {HttpModule} from '@angular/http';
import {HashtagService} from '../services/hashtag.service';
import {LoginComponent} from '../components/login.component';
import {CookieService} from 'angular2-cookie/services/cookies.service';
import {AuthService} from '../services/auth.service';
import {ProfileService} from '../services/profile.service';
import {ProfileComponent} from '../components/profile.component';
import {SignupComponent} from '../components/signup.component';
import {PageNotFoundComponent} from '../components/page-not-found.component';
import {ProfileCardComponent} from '../components/profile-card.component';
import {ViewProfilesComponent} from '../components/view-profiles.component';
import {EditProfileComponent} from '../components/edit-profile.component';
import {CommonModule} from '@angular/common';
import {PostStepsModalComponent} from '../components/post-steps-modal.component';
import {WelcomeComponent} from '../components/welcome.component';
import {AddPostComponent} from '../components/add-post.component';
import {DateDirective} from '../directives/date.directive';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpModule,
    CommonModule
  ],
  declarations: [
    AppComponent,
    HashtagComponent,
    PostsComponent,
    LoginComponent,
    ProfileComponent,
    SignupComponent,
    PageNotFoundComponent,
    ProfileCardComponent,
    ViewProfilesComponent,
    EditProfileComponent,
    PostStepsModalComponent,
    WelcomeComponent,
    AddPostComponent,
    DateDirective
  ],
  providers: [
    PostService,
    HashtagService,
    CookieService,
    AuthService,
    ProfileService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
