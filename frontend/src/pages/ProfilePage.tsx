import { useCallback, useEffect, useMemo, useState } from 'react';
import { getPosts } from '../api/posts';
import { PostCard } from '../components/PostCard';
import type { PostResponse } from '../types/post';
import './ProfilePage.css';

const PROFILE_PAGE_SIZE = 50;

type Props = {
  telegramId: number;
  username?: string;
};

export function ProfilePage({ telegramId, username }: Props) {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getPosts({}, 0, PROFILE_PAGE_SIZE);
      setPosts(res.content);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load profile');
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const displayName = useMemo(() => {
    if (username?.trim()) return `@${username.trim()}`;
    return `id ${telegramId}`;
  }, [telegramId, username]);

  const myPosts = useMemo(() => posts.filter((p) => p.telegramId === telegramId), [posts, telegramId]);

  function handlePostUpdated(updated: PostResponse) {
    setPosts((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
  }

  return (
    <section className="profile">
      <div className="profile__card">
        <div className="profile__top">
          <div className="profile__avatar" aria-hidden="true" />
          <div className="profile__meta">
            <h2 className="profile__name">{displayName}</h2>
            <p className="profile__row">
              <span className="profile__label">Bio</span> Building cool stuff with teammates.
            </p>
            <p className="profile__row">
              <span className="profile__label">Stack</span> React, TypeScript, Spring Boot
            </p>
            <p className="profile__row">
              <span className="profile__label">GitHub</span>{' '}
              <a href="https://github.com/" target="_blank" rel="noreferrer">
                github.com
              </a>
            </p>
          </div>
        </div>
      </div>

      <div className="profile__posts">
        <h3 className="profile__posts-heading">My posts</h3>

        {loading ? <p className="profile__state">Loading…</p> : null}
        {!loading && error ? <p className="profile__state profile__state--error">{error}</p> : null}
        {!loading && !error && myPosts.length === 0 ? <p className="profile__state">No posts yet.</p> : null}

        <ul className="profile__list">
          {myPosts.map((p) => (
            <li key={p.id}>
              <PostCard post={p} onPostUpdated={handlePostUpdated} />
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

