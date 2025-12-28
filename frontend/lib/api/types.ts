export type UserRole = "USER" | "DEVELOPER" | "ADMIN";

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  role: UserRole;
}

export interface CurrentUserResponse {
  userId: string;
  email: string;
  role: UserRole;
}
