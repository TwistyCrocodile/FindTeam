import type {
  ContactInfoResponse,
  CreateUserProfileRequest,
  PublicUserProfileResponse,
  RegisterCurrentUserRequest,
  RegisterUserRequest,
  UpdateContactInfoRequest,
  UpdateUserProfileRequest,
  UserProfileResponse,
} from '../types/user';
import type { PostResponse } from '../types/post';
import { apiFetch, apiUrl, parseErrorMessage, telegramInitDataHeaders } from './client';

type ErrorWithStatus = Error & { status?: number };

function asErrorWithStatus(message: string, status?: number): ErrorWithStatus {
  const err = new Error(message) as ErrorWithStatus;
  err.status = status;
  return err;
}

export async function getPublicUserProfile(telegramId: number): Promise<PublicUserProfileResponse> {
  const res = await fetch(apiUrl(`/api/users/${telegramId}/public-profile`));
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as PublicUserProfileResponse;
}

export async function getPublicUserPosts(telegramId: number): Promise<PostResponse[]> {
  const res = await fetch(apiUrl(`/api/users/${telegramId}/public-posts`));
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as PostResponse[];
}

export async function getUserProfile(telegramId: number): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl(`/api/users/by-telegram/${telegramId}`));
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function getCurrentUserProfile(initData: string): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl('/api/users/me'), {
    headers: telegramInitDataHeaders(initData),
  });
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function registerUser(payload: RegisterUserRequest): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl('/api/users/register'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function registerCurrentUser(
  initData: string,
  payload: RegisterCurrentUserRequest,
): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl('/api/users/me/register'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...telegramInitDataHeaders(initData),
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function createUserProfile(payload: CreateUserProfileRequest): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl('/api/users'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function updateUserProfile(
  telegramId: number,
  payload: UpdateUserProfileRequest,
): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl(`/api/users/${telegramId}`), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function updateCurrentUserProfile(
  initData: string,
  payload: UpdateUserProfileRequest,
): Promise<UserProfileResponse> {
  const res = await apiFetch('/api/users/me', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }, initData);
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as UserProfileResponse;
}

export async function updateUserProfileAuthAware(
  telegramId: number,
  payload: UpdateUserProfileRequest,
  initData?: string | null,
): Promise<UserProfileResponse> {
  return initData ? updateCurrentUserProfile(initData, payload) : updateUserProfile(telegramId, payload);
}

export async function getMyContactInfo(initData: string): Promise<ContactInfoResponse> {
  const res = await apiFetch('/api/users/me/contact', undefined, initData);
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as ContactInfoResponse;
}

export async function updateMyContactInfo(
  initData: string,
  payload: UpdateContactInfoRequest,
): Promise<ContactInfoResponse> {
  const res = await apiFetch('/api/users/me/contact', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }, initData);
  if (!res.ok) {
    const message = await parseErrorMessage(res);
    throw asErrorWithStatus(message, res.status);
  }
  return (await res.json()) as ContactInfoResponse;
}

