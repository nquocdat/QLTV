import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-personal-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './personal-profile.component.html',
  styleUrls: ['./personal-profile.component.css'],
})
export class PersonalProfileComponent implements OnInit {
  currentUser: any = {};
  editMode = false;
  editedUser: any = {};

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.userService.getCurrentUser().subscribe((user) => {
      this.currentUser = user;
      this.editedUser = { ...user };
    });
  }

  enableEdit() {
    this.editMode = true;
    this.editedUser = { ...this.currentUser };
  }

  cancelEdit() {
    this.editMode = false;
  }

  saveEdit() {
    if (!this.editedUser.id) {
      alert('Không tìm thấy ID người dùng!');
      return;
    }
    this.userService.updateUser(this.editedUser.id, this.editedUser).subscribe((res) => {
      this.currentUser = { ...res };
      this.editMode = false;
      alert('Cập nhật thành công!');
    });
  }
}
