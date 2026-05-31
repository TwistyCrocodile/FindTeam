export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface ApplicationResponse {
  id: number;
  postId: number;
  applicantNickname: string;
  applicantTelegramId: number;
  status: ApplicationStatus;
  contactAvailable: boolean;
  createdAt: string;
}

export interface CreateApplicationRequest {
  postId: number;
  telegramId: number;
}
