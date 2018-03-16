import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HashtagComponent } from '../components/hashtags.component';
import {LoginComponent} from '../components/login.component';
import {ProfileComponent} from '../components/profile.component';
import {SignupComponent} from '../components/signup.component';
import {PageNotFoundComponent} from '../components/page-not-found.component';
import {EditProfileComponent} from '../components/edit-profile.component';
import {WelcomeComponent} from '../components/welcome.component';
import {PostsComponent} from 'app/components/posts.component';
import {AddPostComponent} from '../components/add-post.component';

const routes: Routes = [
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },
  { path: 'login',                  component: LoginComponent },
  { path: 'signup',                 component: SignupComponent },
  { path: 'hashtags',               component: HashtagComponent },
  { path: 'edit',                   component: EditProfileComponent },
  { path: 'profile/:display_name',  component: ProfileComponent },
  { path: 'welcome',                component: WelcomeComponent },
  { path: 'posts/:type',            component: PostsComponent },
  { path: 'search/:search',         component: PostsComponent },
  { path: 'add-post',               component: AddPostComponent },
  { path: '**',                     component: PageNotFoundComponent }, // Must be last!
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}
