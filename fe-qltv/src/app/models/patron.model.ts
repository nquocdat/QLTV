export interface Patron {
  id?: number;
  name: string;
  email: string;
  address?: string;
  phoneNumber?: string;
  role?: string;
  isActive?: boolean;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  address?: string;
  phoneNumber?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  name: string;
  email: string;
  role: string;
}
