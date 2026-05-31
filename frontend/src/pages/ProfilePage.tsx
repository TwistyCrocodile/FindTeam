import { useCallback, useEffect, useMemo, useState } from 'react';
import { getPosts } from '../api/posts';
import { updateUserProfile } from '../api/users';
import { PostApplicationsPanel } from '../components/PostApplicationsPanel';
import { PostCard } from '../components/PostCard';
import { UserProfileForm } from '../components/UserProfileForm';
import type { PostResponse } from '../types/post';
import type { UserProfileResponse } from '../types/user';
import './ProfilePage.css';

const PROFILE_PAGE_SIZE = 50;

type Props = {
  telegramId: number;
  profile: UserProfileResponse;
  onProfileUpdated: (profile: UserProfileResponse) => void;
};

export function ProfilePage({ telegramId, profile, onProfileUpdated }: Props) {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [editing, setEditing] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSaving, setProfileSaving] = useState(false);
  const [applicationsPostId, setApplicationsPostId] = useState<number | null>(null);

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

  const myPosts = useMemo(() => posts.filter((p) => p.telegramId === telegramId), [posts, telegramId]);

  function handlePostUpdated(updated: PostResponse) {
    setPosts((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
  }

  function handlePostDeleted(postId: number) {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
  }

  const initialFormValues = useMemo(
    () => ({
      nickname: profile.nickname,
      bio: profile.bio ?? '',
      stack: profile.stack,
      githubUrl: profile.githubUrl ?? '',
    }),
    [profile],
  );

  return (
    <section className="profile">
      <div className="profile__card">
        <div className="profile__top">
          <div className="profile__avatar" aria-hidden="true" />
          <div className="profile__meta">
            <div className="profile__name-row">
              <h2 className="profile__name">{profile.nickname}</h2>
              {!editing ? (
                <button type="button" className="profile__edit-btn" onClick={() => setEditing(true)}>
                  Edit profile
                </button>
              ) : null}
            </div>

            {!editing ? (
              <>
                <p className="profile__row">
                  <span className="profile__label">Bio</span> {profile.bio || '—'}
                </p>
                <p className="profile__row">
                  <span className="profile__label">Stack</span> {profile.stack}
                </p>
                <p className="profile__row">
                  <span className="profile__label">GitHub</span>{' '}
                  {profile.githubUrl ? (
                    <a href={profile.githubUrl} target="_blank" rel="noreferrer">
                      {profile.githubUrl}
                    </a>
                  ) : (
                    '—'
                  )}
                </p>
              </>
            ) : null}
          </div>
        </div>

        {editing ? (
          <div className="profile__edit">
            <UserProfileForm
              initialValues={initialFormValues}
              submitLabel="Save changes"
              loading={profileSaving}
              serverError={profileError}
              onSubmit={async (values) => {
                setProfileError(null);
                setProfileSaving(true);
                try {
                  const updated = await updateUserProfile(telegramId, values);
                  onProfileUpdated(updated);
                  setEditing(false);
                } catch (e) {
                  setProfileError(e instanceof Error ? e.message : 'Request failed');
                } finally {
                  setProfileSaving(false);
                }
              }}
            />
            <div className="profile__edit-actions">
              <button type="button" className="profile__cancel-btn" onClick={() => setEditing(false)} disabled={profileSaving}>
                Cancel
              </button>
            </div>
          </div>
        ) : null}
      </div>

      <div className="profile__posts">
        <h3 className="profile__posts-heading">My posts</h3>

        {loading ? <p className="profile__state">Loading…</p> : null}
        {!loading && error ? <p className="profile__state profile__state--error">{error}</p> : null}
        {!loading && !error && myPosts.length === 0 ? <p className="profile__state">No posts yet.</p> : null}

        <ul className="profile__list">
          {myPosts.map((p) => (
            <li key={p.id}>
              <PostCard
                post={p}
                viewerTelegramId={telegramId}
                showApplicationsButton
                onViewApplications={(postId) =>
                  setApplicationsPostId((current) => (current === postId ? null : postId))
                }
                onPostUpdated={handlePostUpdated}
                onPostDeleted={(postId) => {
                  handlePostDeleted(postId);
                  if (applicationsPostId === postId) setApplicationsPostId(null);
                }}
              />
              {applicationsPostId === p.id ? (
                <PostApplicationsPanel
                  postId={p.id}
                  ownerTelegramId={telegramId}
                  onClose={() => setApplicationsPostId(null)}
                />
              ) : null}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

