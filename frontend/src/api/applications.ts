import type { ApplicationResponse, CreateApplicationRequest } from '../types/application';
import { apiUrl, parseErrorMessage } from './client';

export async function applyToPost(payload: CreateApplicationRequest): Promise<ApplicationResponse> {
  const res = await fetch(apiUrl('/api/applications'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse>;
}

export async function getApplicationsByApplicant(telegramId: number): Promise<ApplicationResponse[]> {
  const res = await fetch(apiUrl(`/api/applications?telegramId=${telegramId}`));
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse[]>;
}

export async function getApplicationsForPost(
  postId: number,
  ownerTelegramId: number,
): Promise<ApplicationResponse[]> {
  const res = await fetch(apiUrl(`/api/posts/${postId}/applications?telegramId=${ownerTelegramId}`));
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse[]>;
}

export async function acceptApplication(applicationId: number, telegramId: number): Promise<ApplicationResponse> {
  const res = await fetch(apiUrl(`/api/applications/${applicationId}/accept?telegramId=${telegramId}`), {
    method: 'PATCH',
  });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse>;
}

export async function rejectApplication(applicationId: number, telegramId: number): Promise<ApplicationResponse> {
  const res = await fetch(apiUrl(`/api/applications/${applicationId}/reject?telegramId=${telegramId}`), {
    method: 'PATCH',
  });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse>;
}
