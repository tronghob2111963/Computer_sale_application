import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UserBuildService, UserBuild } from '../../services/user-build.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-my-builds',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-builds.component.html',
  styleUrls: ['./my-builds.component.scss']
})
export class MyBuildsComponent implements OnInit {
  builds: UserBuild[] = [];
  isLoading = true;

  constructor(
    private buildService: UserBuildService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadUserBuilds();
  }

  loadUserBuilds() {
    const userId = this.authService.getUserIdSafe();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.buildService.getUserBuilds(userId).subscribe({
      next: (response) => {
        this.builds = response.data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading builds:', err);
        this.isLoading = false;
      }
    });
  }

  viewBuild(buildId: string) {
    this.router.navigate(['/pc-builder'], { queryParams: { buildId } });
  }

  deleteBuild(buildId: string) {
    if (!confirm('Bạn có chắc muốn xóa cấu hình này?')) return;

    this.buildService.deleteBuild(buildId).subscribe({
      next: () => {
        this.builds = this.builds.filter(b => b.id !== buildId);
      },
      error: (err) => console.error('Error deleting build:', err)
    });
  }

  createNewBuild() {
    this.router.navigate(['/pc-builder']);
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }
}
