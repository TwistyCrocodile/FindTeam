import type { CreatePostRequest, PostGoal, PostPageResponse, PostResponse, PostStatus, PostType } from '../types/post';
import { apiUrl, parseErrorMessage } from './client';

export interface PostFilters {
  type?: PostType;
  goal?: PostGoal;
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
  if (filters.status) params.set('status', filters.status);
  params.set('page', String(page));
  params.set('size', String(size));

  const q = params.toString();
  const res = await fetch(apiUrl(`/api/posts?${q}`));
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostPageResponse>;
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

/** PATCH /api/posts/{id}/close */
export async function closePost(postId: number): Promise<PostResponse> {
  const res = await fetch(apiUrl(`/api/posts/${postId}/close`), { method: 'PATCH' });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}

/** PATCH /api/posts/{id}/reopen */
export async function reopenPost(postId: number): Promise<PostResponse> {
  const res = await fetch(apiUrl(`/api/posts/${postId}/reopen`), { method: 'PATCH' });
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return res.json() as Promise<PostResponse>;
}
