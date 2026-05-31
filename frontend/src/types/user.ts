export interface CreateUserProfileRequest {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
}

export interface RegisterUserRequest {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
}

export interface UpdateUserProfileRequest {
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
}

export interface UserProfileResponse {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  createdAt: string;
}

