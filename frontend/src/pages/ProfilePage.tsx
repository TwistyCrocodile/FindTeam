import { useCallback, useEffect, useMemo, useState } from 'react';
import { getApplicationsByApplicantAuthAware } from '../api/applications';
import { getCurrentUserPosts, getPosts } from '../api/posts';
import { updateUserProfile } from '../api/users';
import { PostApplicationsPanel } from '../components/PostApplicationsPanel';
import { PostCard } from '../components/PostCard';
import { UserProfileForm } from '../components/UserProfileForm';
import type { ApplicationResponse, ApplicationStatus } from '../types/application';
import type { PostResponse } from '../types/post';
import type { UserProfileResponse } from '../types/user';
import './ProfilePage.css';

const PROFILE_PAGE_SIZE = 50;

function formatWhen(iso: string) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

function formatApplicationStatus(status: ApplicationStatus) {
  if (status === 'PENDING') return 'Pending review';
  if (status === 'ACCEPTED') return 'Accepted';
  return 'Rejected';
}

type Props = {
  telegramId: number;
  initData?: string | null;
  profile: UserProfileResponse;
  onProfileUpdated: (profile: UserProfileResponse) => void;
};

export function ProfilePage({ telegramId, initData, profile, onProfileUpdated }: Props) {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [applicationsLoading, setApplicationsLoading] = useState(true);
  const [applicationsError, setApplicationsError] = useState<string | null>(null);

  const [editing, setEditing] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSaving, setProfileSaving] = useState(false);
  const [applicationsPostId, setApplicationsPostId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      if (initData) {
        const currentUserPosts = await getCurrentUserPosts(initData);
        setPosts(currentUserPosts);
      } else {
        const res = await getPosts({}, 0, PROFILE_PAGE_SIZE);
        setPosts(res.content);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load profile');
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [initData]);

  useEffect(() => {
    void load();
  }, [load]);

  const loadApplications = useCallback(async () => {
    setApplicationsLoading(true);
    setApplicationsError(null);
    try {
      const list = await getApplicationsByApplicantAuthAware(telegramId, initData);
      setApplications(list);
    } catch (e) {
      setApplicationsError(e instanceof Error ? e.message : 'Failed to load applications');
      setApplications([]);
    } finally {
      setApplicationsLoading(false);
    }
  }, [telegramId, initData]);

  useEffect(() => {
    void loadApplications();
  }, [loadApplications]);

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
                initData={initData}
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
                  initData={initData}
                  onClose={() => setApplicationsPostId(null)}
                />
              ) : null}
            </li>
          ))}
        </ul>
      </div>

      <div className="profile__applications">
        <h3 className="profile__posts-heading">My applications</h3>

        {applicationsLoading ? <p className="profile__state">Loading…</p> : null}
        {!applicationsLoading && applicationsError ? (
          <p className="profile__state profile__state--error">{applicationsError}</p>
        ) : null}
        {!applicationsLoading && !applicationsError && applications.length === 0 ? (
          <p className="profile__state">You have not applied to any posts yet.</p>
        ) : null}

        <ul className="profile__application-list">
          {applications.map((application) => (
            <li
              key={application.id}
              className={`profile__application-card profile__application-card--${application.status.toLowerCase()}`}
            >
              <div className="profile__application-head">
                <div className="profile__application-main">
                  <h4 className="profile__application-title">{application.postTitle}</h4>
                  <p className="profile__application-meta">
                    <span>{application.postGoal}</span>
                    <span className="profile__application-dot">·</span>
                    <span>{application.postStatus}</span>
                  </p>
                </div>
                <span className={`profile__application-status profile__application-status--${application.status.toLowerCase()}`}>
                  {formatApplicationStatus(application.status)}
                </span>
              </div>
              <p className="profile__application-time">{formatWhen(application.createdAt)}</p>

              {application.contactAvailable ? (
                <div className="profile__application-contact">
                  <p className="profile__application-contact-note">Contact will be available in a future version.</p>
                  <button type="button" className="profile__application-contact-btn" disabled>
                    Contact (soon)
                  </button>
                </div>
              ) : null}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

