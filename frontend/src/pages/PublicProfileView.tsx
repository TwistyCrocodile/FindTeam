import { useCallback, useEffect, useState } from 'react';
import { getPublicUserPosts, getPublicUserProfile } from '../api/users';
import { getFriendlyErrorMessage } from '../app/errors';
import { getPostGoalLabel, getPostTypeLabel, getUserStatusLabel } from '../app/translations';
import { useLanguage } from '../hooks/useLanguage';
import type { PostResponse } from '../types/post';
import type { PublicUserProfileResponse } from '../types/user';
import './PublicProfileView.css';

type Props = {
  telegramId: number;
  onBack: () => void;
};

function formatWhen(iso: string) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

export function PublicProfileView({ telegramId, onBack }: Props) {
  const { t } = useLanguage();
  const [profile, setProfile] = useState<PublicUserProfileResponse | null>(null);
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [p, list] = await Promise.all([
        getPublicUserProfile(telegramId),
        getPublicUserPosts(telegramId),
      ]);
      setProfile(p);
      setPosts(list);
    } catch (e) {
      setProfile(null);
      setPosts([]);
      setError(getFriendlyErrorMessage(e, t.publicProfile.failedToLoadProfile, t));
    } finally {
      setLoading(false);
    }
  }, [telegramId, t.publicProfile.failedToLoadProfile]);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <section className="public-profile">
      <button type="button" className="public-profile__back" onClick={onBack}>
        ← {t.common.back}
      </button>

      <h2 className="public-profile__heading">{t.publicProfile.publicProfile}</h2>

      {loading ? <p className="public-profile__state">{t.common.loadingProfile}</p> : null}

      {!loading && error ? (
        <p className="public-profile__state public-profile__state--error">{error}</p>
      ) : null}

      {!loading && !error && profile ? (
        <>
          <div className="public-profile__card">
            <div className="public-profile__avatar" aria-hidden="true" />
            <h2 className="public-profile__name">{profile.nickname}</h2>
            <p className="public-profile__row">
              <span className="public-profile__label">{t.profile.bio}</span> {profile.bio || '—'}
            </p>
            <p className="public-profile__row">
              <span className="public-profile__label">{t.profile.stack}</span> {profile.stack}
            </p>
            <p className="public-profile__row">
              <span className="public-profile__label">{t.status.label}</span> {getUserStatusLabel(t, profile.status)}
            </p>
            <p className="public-profile__row">
              <span className="public-profile__label">{t.profile.github}</span>{' '}
              {profile.githubUrl ? (
                <a href={profile.githubUrl} target="_blank" rel="noreferrer">
                  {profile.githubUrl}
                </a>
              ) : (
                '—'
              )}
            </p>
            <p className="public-profile__meta">{t.publicProfile.memberSince} {formatWhen(profile.createdAt)}</p>
          </div>

          <div className="public-profile__posts">
            <h3 className="public-profile__posts-heading">{t.publicProfile.activePosts}</h3>
            <p className="public-profile__posts-hint">{t.publicProfile.activePostsHint}</p>

            {posts.length === 0 ? (
              <p className="public-profile__state">{t.publicProfile.noActivePosts}</p>
            ) : (
              <ul className="public-profile__posts-list">
                {posts.map((post) => (
                  <li key={post.id} className="public-profile__post">
                    <div className="public-profile__post-head">
                      <h4 className="public-profile__post-title">{post.title}</h4>
                      <span className="public-profile__post-pill">{getPostTypeLabel(t, post.type)}</span>
                    </div>
                    <p className="public-profile__post-meta">
                      <span>{getPostGoalLabel(t, post.goal)}</span>
                      <span className="public-profile__dot">·</span>
                      <span>{formatWhen(post.createdAt)}</span>
                    </p>
                    <p className="public-profile__post-stack">
                      <span className="public-profile__label">{t.profile.stack}</span> {post.stack}
                    </p>
                    <p className="public-profile__post-description">{post.description}</p>
                    {post.eventLink ? (
                      <p className="public-profile__post-link">
                        <a href={post.eventLink} target="_blank" rel="noreferrer">
                          {post.eventLink}
                        </a>
                      </p>
                    ) : null}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      ) : null}
    </section>
  );
}
