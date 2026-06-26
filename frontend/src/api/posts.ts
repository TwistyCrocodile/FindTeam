import type {
  CreateCurrentUserPostRequest,
  CreatePostRequest,
  PostGoal,
  PostLanguage,
  PostPageResponse,
  PostResponse,
  PostStatus,
  PostType,
} from '../types/post';
import { apiError, apiFetch, apiUrl, parseErrorMessage } from './client';

export interface PostFilters {
  type?: PostType;
  goal?: PostGoal;
  language?: PostLanguage;
  status?: PostStatus;
}

/**
 * GET /api/posts — paginated, optional filters.
 */
export async function getPosts(
  filters: PostFilters,
  page: number,
  size: number,
): Promise<PostPageResponse> {
  const params = new URLSearchParams();
  if (filters.type) params.set('type', filters.type);
  if (filters.goal) params.set('goal', filters.goal);
  if (filters.language) params.set('language', filters.language);
  if (filters.status) params.set('status', filters.status);
  params.set('page', String(page));
  params.set('size', String(size));

  const q = params.toString();
  const res = await fetch(apiUrl(`/api/posts?${q}`));
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostPageResponse>;
}

/** GET /api/posts/me */
export async function getCurrentUserPosts(initData: string): Promise<PostResponse[]> {
  const res = await apiFetch('/api/posts/me', undefined, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse[]>;
}

/** POST /api/posts */
export async function createPost(payload: CreatePostRequest): Promise<PostResponse> {
  const res = await fetch(apiUrl('/api/posts'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** POST /api/posts/me */
export async function createPostForCurrentUser(
  payload: CreateCurrentUserPostRequest,
  initData: string,
): Promise<PostResponse> {
  const res = await apiFetch('/api/posts/me', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** PATCH /api/posts/{id}/close */
export async function closePost(postId: number, telegramId: number): Promise<PostResponse> {
  const res = await fetch(apiUrl(`/api/posts/${postId}/close?telegramId=${telegramId}`), { method: 'PATCH' });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** PATCH /api/posts/{id}/close-secure */
export async function closePostSecure(postId: number, initData: string): Promise<PostResponse> {
  const res = await apiFetch(`/api/posts/${postId}/close-secure`, { method: 'PATCH' }, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** PATCH /api/posts/{id}/reopen */
export async function reopenPost(postId: number, telegramId: number): Promise<PostResponse> {
  const res = await fetch(apiUrl(`/api/posts/${postId}/reopen?telegramId=${telegramId}`), { method: 'PATCH' });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** PATCH /api/posts/{id}/reopen-secure */
export async function reopenPostSecure(postId: number, initData: string): Promise<PostResponse> {
  const res = await apiFetch(`/api/posts/${postId}/reopen-secure`, { method: 'PATCH' }, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** DELETE /api/posts/{id} */
export async function deletePost(postId: number, telegramId: number): Promise<void> {
  const params = new URLSearchParams({ telegramId: String(telegramId) });
  const res = await fetch(apiUrl(`/api/posts/${postId}?${params.toString()}`), { method: 'DELETE' });
  if (!res.ok) throw apiError(await parseErrorMessage(res), res);
}

/** DELETE /api/posts/{id}/secure */
export async function deletePostSecure(postId: number, initData: string): Promise<void> {
  const res = await apiFetch(`/api/posts/${postId}/secure`, { method: 'DELETE' }, initData);
  if (!res.ok) throw apiError(await parseErrorMessage(res), res);
}

/** PUT /api/posts/{id} */
export async function updatePost(
  postId: number,
  telegramId: number,
  payload: CreateCurrentUserPostRequest,
): Promise<PostResponse> {
  const res = await fetch(apiUrl(`/api/posts/${postId}?telegramId=${telegramId}`), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** PUT /api/posts/{id} with Telegram init data */
export async function updatePostForCurrentUser(
  postId: number,
  payload: CreateCurrentUserPostRequest,
  initData: string,
): Promise<PostResponse> {
  const res = await apiFetch(`/api/posts/${postId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }, initData);
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

export async function createPostAuthAware(
  payload: CreatePostRequest,
  initData?: string | null,
): Promise<PostResponse> {
  if (initData) {
    const currentUserPayload: CreateCurrentUserPostRequest = {
      type: payload.type,
      title: payload.title,
      description: payload.description,
      stack: payload.stack,
      goal: payload.goal,
      eventLink: payload.eventLink,
    };
    return createPostForCurrentUser(currentUserPayload, initData);
  }
  return createPost(payload);
}

export async function closePostAuthAware(
  postId: number,
  telegramId: number,
  initData?: string | null,
): Promise<PostResponse> {
  return initData ? closePostSecure(postId, initData) : closePost(postId, telegramId);
}

export async function reopenPostAuthAware(
  postId: number,
  telegramId: number,
  initData?: string | null,
): Promise<PostResponse> {
  return initData ? reopenPostSecure(postId, initData) : reopenPost(postId, telegramId);
}

export async function deletePostAuthAware(
  postId: number,
  telegramId: number,
  initData?: string | null,
): Promise<void> {
  return initData ? deletePostSecure(postId, initData) : deletePost(postId, telegramId);
}

export async function updatePostAuthAware(
  postId: number,
  telegramId: number,
  payload: CreateCurrentUserPostRequest,
  initData?: string | null,
): Promise<PostResponse> {
  return initData
    ? updatePostForCurrentUser(postId, payload, initData)
    : updatePost(postId, telegramId, payload);
}
