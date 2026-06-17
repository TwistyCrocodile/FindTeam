import type { UserStatus } from './user';

/** Mirrors backend enums (Jackson serializes as these string names). */
export type PostType = 'SEEKING_TEAM' | 'SEEKING_MEMBER';

export type PostGoal = 'HACKATHON' | 'PET_PROJECT' | 'STARTUP' | 'JOB' | 'OLYMPIAD';

export type PostStatus = 'ACTIVE' | 'CLOSED';

export type PostLanguage = 'RU' | 'EN';

export interface PostResponse {
  id: number;
  telegramId: number;
  nickname: string | null;
  authorStatus: UserStatus;
  type: PostType;
  title: string;
  description: string;
  language?: PostLanguage | null;
  stack: string;
  goal: PostGoal;
  status: PostStatus;
  eventLink: string | null;
  /** ISO-8601 string from backend LocalDateTime */
  createdAt: string;
}

export interface PostPageResponse {
  content: PostResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CreatePostRequest {
  telegramId: number;
  type: PostType;
  title: string;
  description: string;
  stack: string;
  goal: PostGoal;
  eventLink?: string;
}

export type CreateCurrentUserPostRequest = Omit<CreatePostRequest, 'telegramId'>;
