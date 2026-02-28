export interface User {
  id?: number;
  name: string;
  surname: string;
  email: string;
  password?: string;
  userTypesId: number; // FK hacia UserTypes
}

export interface AuthResponse {
  token: string;
  user: User;
}