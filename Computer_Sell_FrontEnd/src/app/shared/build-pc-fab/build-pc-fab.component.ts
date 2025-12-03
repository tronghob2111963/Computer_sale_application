import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
    selector: 'app-build-pc-fab',
    standalone: true,
    imports: [CommonModule],
    template: `
    <button 
      (click)="navigateToBuildPC()"
      class="build-pc-fab"
      title="Tự build PC">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
        <line x1="8" y1="21" x2="16" y2="21"></line>
        <line x1="12" y1="17" x2="12" y2="21"></line>
      </svg>
      <span class="fab-text">Build PC</span>
    </button>
  `,
    styles: [`
    .build-pc-fab {
      position: fixed;
      bottom: 80px;
      right: 20px;
      z-index: 999;
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      color: white;
      border: none;
      border-radius: 50px;
      padding: 12px 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      box-shadow: 0 4px 20px rgba(16, 185, 129, 0.4);
      cursor: pointer;
      transition: all 0.3s ease;
      font-weight: 600;
      font-size: 14px;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 6px 25px rgba(16, 185, 129, 0.5);
        background: linear-gradient(135deg, #059669 0%, #047857 100%);
      }
      
      &:active {
        transform: translateY(-1px);
      }
      
      svg {
        width: 24px;
        height: 24px;
      }
      
      .fab-text {
        display: none;
        
        @media (min-width: 640px) {
          display: inline;
        }
      }
      
      @media (max-width: 639px) {
        width: 56px;
        height: 56px;
        padding: 0;
        justify-content: center;
        border-radius: 50%;
        bottom: 70px;
      }
    }
    
    @keyframes pulse {
      0%, 100% {
        box-shadow: 0 4px 20px rgba(16, 185, 129, 0.4);
      }
      50% {
        box-shadow: 0 4px 30px rgba(16, 185, 129, 0.6);
      }
    }
    
    .build-pc-fab {
      animation: pulse 2s infinite;
    }
  `]
})
export class BuildPcFabComponent {
    constructor(private router: Router) { }

    navigateToBuildPC() {
        this.router.navigate(['/pc-builder']);
    }
}
