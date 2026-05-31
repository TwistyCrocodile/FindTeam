import type {
  ApplicationResponse,
  CreateApplicationRequest,
  CreateCurrentUserApplicationRequest,
} from '../types/application';
import type { ContactInfoResponse } from '../types/user';
import { apiFetch, apiUrl, parseErrorMessage } from './client';

export async function applyToPost(payload: CreateApplicationRequest): Promise<ApplicationResponse> {
  const res = await fetch(apiUrl('/api/applications'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse>;
}

export async function applyToPostForCurrentUser(
  payload: CreateCurrentUserApplicationRequest,
  initData: string,
): Promise<ApplicationResponse> {
  const res = await apiFetch('/api/applications/me', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse>;
}

export async function getApplicationsByApplicant(telegramId: number): Promise<ApplicationResponse[]> {
  const res = await fetch(apiUrl(`/api/applications?telegramId=${telegramId}`));
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse[]>;
}

export async function getCurrentUserApplications(initData: string): Promise<ApplicationResponse[]> {
  const res = await apiFetch('/api/applications/me', undefined, initData);
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

export async function getApplicationsForPostSecure(
  postId: number,
  initData: string,
): Promise<ApplicationResponse[]> {
  const res = await apiFetch(`/api/posts/${postId}/applications/secure`, undefined, initData);
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

export async function acceptApplicationSecure(applicationId: number, initData: string): Promise<ApplicationResponse> {
  const res = await apiFetch(`/api/applications/${applicationId}/accept-secure`, {
    method: 'PATCH',
  }, initData);
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

export async function rejectApplicationSecure(applicationId: number, initData: string): Promise<ApplicationResponse> {
  const res = await apiFetch(`/api/applications/${applicationId}/reject-secure`, {
    method: 'PATCH',
  }, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ApplicationResponse>;
}

export async function applyToPostAuthAware(
  payload: CreateApplicationRequest,
  initData?: string | null,
): Promise<ApplicationResponse> {
  return initData ? applyToPostForCurrentUser({ postId: payload.postId }, initData) : applyToPost(payload);
}

export async function getApplicationsByApplicantAuthAware(
  telegramId: number,
  initData?: string | null,
): Promise<ApplicationResponse[]> {
  return initData ? getCurrentUserApplications(initData) : getApplicationsByApplicant(telegramId);
}

export async function getApplicationsForPostAuthAware(
  postId: number,
  ownerTelegramId: number,
  initData?: string | null,
): Promise<ApplicationResponse[]> {
  return initData ? getApplicationsForPostSecure(postId, initData) : getApplicationsForPost(postId, ownerTelegramId);
}

export async function acceptApplicationAuthAware(
  applicationId: number,
  telegramId: number,
  initData?: string | null,
): Promise<ApplicationResponse> {
  return initData ? acceptApplicationSecure(applicationId, initData) : acceptApplication(applicationId, telegramId);
}

export async function rejectApplicationAuthAware(
  applicationId: number,
  telegramId: number,
  initData?: string | null,
): Promise<ApplicationResponse> {
  return initData ? rejectApplicationSecure(applicationId, initData) : rejectApplication(applicationId, telegramId);
}

export async function getApplicationContact(applicationId: number, initData: string): Promise<ContactInfoResponse> {
  const res = await apiFetch(`/api/applications/${applicationId}/contact-secure`, undefined, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<ContactInfoResponse>;
}
