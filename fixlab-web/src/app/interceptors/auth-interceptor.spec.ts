import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthInterceptor } from './auth-interceptor';
import { AuthService } from '../services/auth';

describe('AuthInterceptor', () => {
  const mockAuthService = { getToken: () => null };
  const mockRouter = { navigate: () => {} };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthInterceptor,
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  it('should be created', () => {
    const interceptor = TestBed.inject(AuthInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
