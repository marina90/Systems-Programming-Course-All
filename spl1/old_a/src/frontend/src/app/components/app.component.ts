import {Component, OnInit} from '@angular/core';
import {AuthService} from '../services/auth.service';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './html/app.component.html',
  styleUrls: ['./css/app.component.css']
})

export class AppComponent implements OnInit {
  title = 'Instagram but better :)';
  displayName = null;
  searchQuery : string = "";

  constructor(
    private authService: AuthService,
    private router: Router) {}

  ngOnInit(): void {
    this.refreshDisplayName();
  }

  logout(): void {
    this.authService.logout();
    this.refreshDisplayName();
  }

  search(): void {
    this.router.navigate(["search", this.searchQuery]);
  }

  private refreshDisplayName(): void {
    this.displayName = this.authService.getDisplayName();
  }

}
