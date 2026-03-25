import type { CreateUserProfileRequest, UpdateUserProfileRequest, UserProfileResponse } from '../types/user';
import { apiUrl, parseErrorMessage } from './client';

type ErrorWithStatus = Error & { status?: number };

function asErrorWithStatus(message: string, status?: number): ErrorWithStatus {
  const err = new Error(message) as ErrorWithStatus;
  err.status = status;
  return err;
}

export async function getUserProfile(telegramId: number): Promise<UserProfileResponse> {
  const res = await fetch(apiUrl(`/api/users/${telegramId}`));
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

