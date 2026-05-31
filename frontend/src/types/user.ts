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

export interface RegisterCurrentUserRequest {
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

/** Public profile only — no contact unlock fields. */
export interface PublicUserProfileResponse {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  createdAt: string;
}

export interface UpdateContactInfoRequest {
  contactTelegramUsername?: string;
  contactGithubUrl?: string;
  contactEmail?: string;
}

export interface ContactInfoResponse {
  contactTelegramUsername?: string | null;
  contactGithubUrl?: string | null;
  contactEmail?: string | null;
}

