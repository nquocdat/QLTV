import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authToken = authService.getToken();

  console.log('Auth Interceptor:', {
    url: req.url,
    method: req.method,
    hasToken: !!authToken,
    token: authToken ? `${authToken.substring(0, 20)}...` : 'None',
  });

  // Luôn thêm Authorization header nếu có token
  if (authToken) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${authToken}`),
    });
    console.log('Auth Interceptor: Added Authorization header');
    return next(authReq);
  }

  console.log('Auth Interceptor: No token, proceeding without auth');
  return next(req);
};
