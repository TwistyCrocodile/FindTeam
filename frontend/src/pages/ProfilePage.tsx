import { type FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import { getApplicationContact, getApplicationsByApplicantAuthAware } from '../api/applications';
import { getCurrentUserPosts, getPosts } from '../api/posts';
import { getMyContactInfo, updateMyContactInfo, updateUserProfileAuthAware } from '../api/users';
import { ContactInfoView } from '../components/ContactInfoView';
import { PostApplicationsPanel } from '../components/PostApplicationsPanel';
import { PostCard } from '../components/PostCard';
import { UserProfileForm } from '../components/UserProfileForm';
import { useTheme } from '../hooks/useTheme';
import type { ApplicationResponse, ApplicationStatus } from '../types/application';
import type { PostResponse } from '../types/post';
import type { ContactInfoResponse, UserProfileResponse } from '../types/user';
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
  onViewUserProfile: (telegramId: number) => void;
};

export function ProfilePage({ telegramId, initData, profile, onProfileUpdated, onViewUserProfile }: Props) {
  const { theme, setTheme } = useTheme();
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
  const [contactInfo, setContactInfo] = useState<ContactInfoResponse>({
    contactTelegramUsername: '',
    contactGithubUrl: '',
    contactEmail: '',
  });
  const [contactDraft, setContactDraft] = useState({
    contactTelegramUsername: '',
    contactGithubUrl: '',
    contactEmail: '',
  });
  const [contactLoading, setContactLoading] = useState(false);
  const [contactSaving, setContactSaving] = useState(false);
  const [contactMessage, setContactMessage] = useState<string | null>(null);
  const [unlockedContacts, setUnlockedContacts] = useState<Record<number, ContactInfoResponse>>({});
  const [contactBusyId, setContactBusyId] = useState<number | null>(null);
  const [contactErrorById, setContactErrorById] = useState<Record<number, string>>({});

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

  useEffect(() => {
    if (!initData) return;

    let cancelled = false;
    async function loadContactInfo() {
      setContactLoading(true);
      setContactMessage(null);
      try {
        const info = await getMyContactInfo(initData as string);
        if (cancelled) return;
        const draft = {
          contactTelegramUsername: info.contactTelegramUsername ?? '',
          contactGithubUrl: info.contactGithubUrl ?? '',
          contactEmail: info.contactEmail ?? '',
        };
        setContactInfo(info);
        setContactDraft(draft);
      } catch (e) {
        if (!cancelled) setContactMessage(e instanceof Error ? e.message : 'Failed to load contact info');
      } finally {
        if (!cancelled) setContactLoading(false);
      }
    }

    void loadContactInfo();
    return () => {
      cancelled = true;
    };
  }, [initData]);

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

  async function handleContactSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!initData) return;

    setContactSaving(true);
    setContactMessage(null);
    try {
      const updated = await updateMyContactInfo(initData, {
        contactTelegramUsername: contactDraft.contactTelegramUsername.trim().replace(/^@/, ''),
        contactGithubUrl: contactDraft.contactGithubUrl.trim(),
        contactEmail: contactDraft.contactEmail.trim(),
      });
      setContactInfo(updated);
      setContactDraft({
        contactTelegramUsername: updated.contactTelegramUsername ?? '',
        contactGithubUrl: updated.contactGithubUrl ?? '',
        contactEmail: updated.contactEmail ?? '',
      });
      setContactMessage('Contact info saved.');
    } catch (e) {
      setContactMessage(e instanceof Error ? e.message : 'Could not save contact info');
    } finally {
      setContactSaving(false);
    }
  }

  async function handleViewContact(applicationId: number) {
    if (!initData) {
      setContactErrorById((prev) => ({ ...prev, [applicationId]: 'Open in Telegram to view contact.' }));
      return;
    }

    setContactBusyId(applicationId);
    setContactErrorById((prev) => ({ ...prev, [applicationId]: '' }));
    try {
      const contact = await getApplicationContact(applicationId, initData);
      setUnlockedContacts((prev) => ({ ...prev, [applicationId]: contact }));
    } catch (e) {
      setContactErrorById((prev) => ({
        ...prev,
        [applicationId]: e instanceof Error ? e.message : 'Could not load contact info',
      }));
    } finally {
      setContactBusyId(null);
    }
  }

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
                  const updated = await updateUserProfileAuthAware(telegramId, values, initData);
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

      <div className="profile__theme-card">
        <h3 className="profile__posts-heading">Theme</h3>
        <div className="profile__theme-options" role="group" aria-label="Theme">
          <button
            type="button"
            className={`profile__theme-btn ${theme === 'dark' ? 'profile__theme-btn--active' : ''}`}
            onClick={() => setTheme('dark')}
          >
            Dark
          </button>
          <button
            type="button"
            className={`profile__theme-btn ${theme === 'light' ? 'profile__theme-btn--active' : ''}`}
            onClick={() => setTheme('light')}
          >
            Light
          </button>
        </div>
      </div>

      <div className="profile__contact-card">
        <h3 className="profile__posts-heading">Contact info</h3>
        <p className="profile__contact-hint">
          Contact info is shared only after an application is accepted and only with the accepted application pair.
        </p>

        {!initData ? (
          <p className="profile__state">Contact editing requires Telegram Mini App mode.</p>
        ) : (
          <>
            {contactLoading ? <p className="profile__state">Loading contact info…</p> : null}
            <form className="profile__contact-form" onSubmit={handleContactSubmit}>
              <label className="profile__contact-field">
                <span>Telegram username</span>
                <input
                  value={contactDraft.contactTelegramUsername}
                  onChange={(e) =>
                    setContactDraft((prev) => ({ ...prev, contactTelegramUsername: e.target.value }))
                  }
                  placeholder="username"
                  maxLength={32}
                />
              </label>
              <label className="profile__contact-field">
                <span>GitHub URL</span>
                <input
                  value={contactDraft.contactGithubUrl}
                  onChange={(e) => setContactDraft((prev) => ({ ...prev, contactGithubUrl: e.target.value }))}
                  placeholder="https://github.com/username"
                  maxLength={255}
                />
              </label>
              <label className="profile__contact-field">
                <span>Email</span>
                <input
                  type="email"
                  value={contactDraft.contactEmail}
                  onChange={(e) => setContactDraft((prev) => ({ ...prev, contactEmail: e.target.value }))}
                  placeholder="you@example.com"
                  maxLength={255}
                />
              </label>
              <button type="submit" className="profile__contact-save" disabled={contactSaving}>
                {contactSaving ? 'Saving…' : 'Save contact info'}
              </button>
            </form>
            {contactMessage ? (
              <p className={`profile__state ${contactMessage.endsWith('saved.') ? '' : 'profile__state--error'}`}>
                {contactMessage}
              </p>
            ) : null}
            <div className="profile__contact-preview">
              <h4 className="profile__contact-preview-title">Current contact info</h4>
              <ContactInfoView contact={contactInfo} emptyMessage="You have not added contact info yet." />
            </div>
          </>
        )}
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
                  onViewUserProfile={onViewUserProfile}
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
                  <button
                    type="button"
                    className="profile__application-contact-btn"
                    disabled={contactBusyId !== null}
                    onClick={() => void handleViewContact(application.id)}
                  >
                    {contactBusyId === application.id ? 'Loading…' : 'View contact'}
                  </button>
                  {contactErrorById[application.id] ? (
                    <p className="profile__application-contact-note profile__application-contact-note--error">
                      {contactErrorById[application.id]}
                    </p>
                  ) : null}
                  {unlockedContacts[application.id] ? (
                    <ContactInfoView contact={unlockedContacts[application.id]} />
                  ) : null}
                </div>
              ) : null}
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

