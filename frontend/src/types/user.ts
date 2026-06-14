export type UserStatus = 'LOOKING_FOR_TEAM' | 'LOOKING_FOR_PROJECT' | 'OPEN_TO_OFFERS' | 'BUSY';

export interface CreateUserProfileRequest {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  status?: UserStatus;
}

export interface RegisterUserRequest {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  status?: UserStatus;
}

export interface RegisterCurrentUserRequest {
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  status?: UserStatus;
}

export interface UpdateUserProfileRequest {
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  status?: UserStatus;
}

export interface UserProfileResponse {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  status: UserStatus;
  createdAt: string;
}

/** Public profile only — no contact unlock fields. */
export interface PublicUserProfileResponse {
  telegramId: number;
  nickname: string;
  bio?: string;
  stack: string;
  githubUrl?: string;
  status: UserStatus;
  createdAt: string;
}

export interface UpdateContactInfoRequest {
  contactTelegramUsername?: string;
  contactEmail?: string;
}

export interface ContactInfoResponse {
  contactTelegramUsername?: string | null;
  contactEmail?: string | null;
}
