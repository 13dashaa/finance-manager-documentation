import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router'; // <--- Импортируйте ЭТИ

@Component({
  selector: 'app-header', // Убедитесь, что селектор правильный
  standalone: true,
  imports: [
    RouterLink,         // <--- Добавьте сюда
    RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

}
