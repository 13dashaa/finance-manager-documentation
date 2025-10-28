// home.component.ts
import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ]
})
export class HomeComponent {

}
