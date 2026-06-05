import type { PostGoal, PostStatus } from './post';
import type { UserStatus } from './user';

export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface ApplicationResponse {
  id: number;
  postId: number;
  postTitle: string;
  postGoal: PostGoal;
  postStatus: PostStatus;
  applicantNickname: string;
  applicantTelegramId: number;
  applicantStatus: UserStatus;
  status: ApplicationStatus;
  contactAvailable: boolean;
  createdAt: string;
}

export interface CreateApplicationRequest {
  postId: number;
  telegramId: number;
}

export interface CreateCurrentUserApplicationRequest {
  postId: number;
}
