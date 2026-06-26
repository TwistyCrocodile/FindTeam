import { type FormEvent, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { getApplicationContact, getApplicationsByApplicantAuthAware } from '../api/applications';
import { getCurrentUserPosts, getPosts } from '../api/posts';
import { getMyContactInfo, updateMyContactInfo, updateUserProfileAuthAware } from '../api/users';
import { getFriendlyErrorMessage } from '../app/errors';
import { getApplicationStatusLabel, getPostGoalLabel, getPostStatusLabel, getUserStatusLabel } from '../app/translations';
import { ContactInfoView, hasContactInfo as hasUnlockedContactInfo } from '../components/ContactInfoView';
import { PostApplicationsPanel } from '../components/PostApplicationsPanel';
import { PostCard } from '../components/PostCard';
import { UserProfileForm } from '../components/UserProfileForm';
import { useLanguage } from '../hooks/useLanguage';
import { useTheme } from '../hooks/useTheme';
import type { ApplicationResponse } from '../types/application';
import type { PostResponse } from '../types/post';
import type { ContactInfoResponse, UserProfileResponse } from '../types/user';
import { hasAnyContact } from '../utils/contactInfo';
import './ProfilePage.css';

const PROFILE_PAGE_SIZE = 50;
const DEVELOPER_CONTACT_URL = 'https://t.me/pisnotequaltonp';

function formatWhen(iso: string) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

type Props = {
  telegramId: number;
  initData?: string | null;
  profile: UserProfileResponse;
  onProfileUpdated: (profile: UserProfileResponse) => void;
  contactInfoSnapshot?: ContactInfoResponse | null;
  onContactInfoUpdated?: (contactInfo: ContactInfoResponse) => void;
  contactFocusToken?: number;
  onViewUserProfile: (telegramId: number) => void;
};

export function ProfilePage({
  telegramId,
  initData,
  profile,
  onProfileUpdated,
  contactInfoSnapshot,
  onContactInfoUpdated,
  contactFocusToken = 0,
  onViewUserProfile,
}: Props) {
  const { language, setLanguage, t } = useLanguage();
  const { theme, setTheme } = useTheme();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [applicationsLoading, setApplicationsLoading] = useState(true);
  const [applicationsError, setApplicationsError] = useState<string | null>(null);

  const [editing, setEditing] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileMessage, setProfileMessage] = useState<string | null>(null);
  const [profileSaving, setProfileSaving] = useState(false);
  const [applicationsPostId, setApplicationsPostId] = useState<number | null>(null);
  const [contactInfo, setContactInfo] = useState<ContactInfoResponse>({
    contactTelegramUsername: '',
    contactEmail: '',
  });
  const [contactDraft, setContactDraft] = useState({
    contactTelegramUsername: '',
    contactEmail: '',
  });
  const [contactLoading, setContactLoading] = useState(false);
  const [contactSaving, setContactSaving] = useState(false);
  const [contactMessage, setContactMessage] = useState<string | null>(null);
  const [unlockedContacts, setUnlockedContacts] = useState<Record<number, ContactInfoResponse>>({});
  const [contactBusyId, setContactBusyId] = useState<number | null>(null);
  const [contactErrorById, setContactErrorById] = useState<Record<number, string>>({});
  const contactCardRef = useRef<HTMLDivElement | null>(null);
  const telegramContactInputRef = useRef<HTMLInputElement | null>(null);

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
      setError(getFriendlyErrorMessage(e, t.profile.failedToLoadProfile, t));
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [initData, t.profile.failedToLoadProfile]);

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
      setApplicationsError(getFriendlyErrorMessage(e, t.profile.failedToLoadApplications, t));
      setApplications([]);
    } finally {
      setApplicationsLoading(false);
    }
  }, [telegramId, initData, t.profile.failedToLoadApplications]);

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
          contactEmail: info.contactEmail ?? '',
        };
        setContactInfo(info);
        setContactDraft(draft);
        onContactInfoUpdated?.(info);
      } catch (e) {
        if (!cancelled) setContactMessage(getFriendlyErrorMessage(e, t.contact.couldNotLoadContact, t));
      } finally {
        if (!cancelled) setContactLoading(false);
      }
    }

    void loadContactInfo();
    return () => {
      cancelled = true;
    };
  }, [initData, onContactInfoUpdated, t.contact.couldNotLoadContact]);

  useEffect(() => {
    if (!contactInfoSnapshot) return;
    setContactInfo(contactInfoSnapshot);
    setContactDraft({
      contactTelegramUsername: contactInfoSnapshot.contactTelegramUsername ?? '',
      contactEmail: contactInfoSnapshot.contactEmail ?? '',
    });
  }, [contactInfoSnapshot]);

  useEffect(() => {
    if (contactFocusToken === 0) return;
    focusContactFields();
  }, [contactFocusToken]);

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
      status: profile.status ?? 'OPEN_TO_OFFERS',
    }),
    [profile],
  );

  const hasContactInfo = useMemo(() => hasAnyContact(profile, contactInfo), [profile, contactInfo]);

  function focusContactFields() {
    contactCardRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    window.setTimeout(() => telegramContactInputRef.current?.focus(), 250);
  }

  async function handleContactSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!initData) return;

    setContactSaving(true);
    setContactMessage(null);
    try {
      const updated = await updateMyContactInfo(initData, {
        contactTelegramUsername: contactDraft.contactTelegramUsername.trim().replace(/^@/, ''),
        contactEmail: contactDraft.contactEmail.trim(),
      });
      setContactInfo(updated);
      onContactInfoUpdated?.(updated);
      setContactDraft({
        contactTelegramUsername: updated.contactTelegramUsername ?? '',
        contactEmail: updated.contactEmail ?? '',
      });
      setContactMessage(t.contact.contactInfoSaved);
    } catch (e) {
      setContactMessage(getFriendlyErrorMessage(e, t.contact.couldNotSaveContact, t));
    } finally {
      setContactSaving(false);
    }
  }

  async function handleViewContact(applicationId: number) {
    if (!initData) {
      setContactErrorById((prev) => ({ ...prev, [applicationId]: t.common.openInTelegram }));
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
        [applicationId]: getFriendlyErrorMessage(e, t.contact.couldNotLoadContact, t),
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
                  {t.profile.editProfile}
                </button>
              ) : null}
            </div>

            {!editing ? (
              <>
                <p className="profile__row">
                  <span className="profile__label">{t.profile.bio}</span> {profile.bio || '—'}
                </p>
                <p className="profile__row profile__row--stack">
                  <span className="profile__label">{t.profile.stack}</span> {profile.stack}
                </p>
                <p className="profile__row">
                  <span className="profile__label">{t.status.label}</span> {getUserStatusLabel(t, profile.status)}
                </p>
                <p className="profile__row">
                  <span className="profile__label">{t.profile.github}</span>{' '}
                  {profile.githubUrl ? (
                    <a href={profile.githubUrl} target="_blank" rel="noopener noreferrer">
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
              submitLabel={t.common.saveChanges}
              loading={profileSaving}
              serverError={profileError}
              onSubmit={async (values) => {
                setProfileError(null);
                setProfileMessage(null);
                setProfileSaving(true);
                try {
                  const updated = await updateUserProfileAuthAware(telegramId, values, initData);
                  onProfileUpdated(updated);
                  setProfileMessage(t.profile.profileUpdated);
                  setEditing(false);
                } catch (e) {
                  setProfileError(getFriendlyErrorMessage(e, t.common.requestFailed, t));
                } finally {
                  setProfileSaving(false);
                }
              }}
            />
            <div className="profile__edit-actions">
              <button type="button" className="profile__cancel-btn" onClick={() => setEditing(false)} disabled={profileSaving}>
                {t.common.cancel}
              </button>
            </div>
          </div>
        ) : null}
        {profileMessage && !editing ? <p className="profile__state profile__state--success">{profileMessage}</p> : null}
      </div>

      <div className="profile__theme-card">
        <h3 className="profile__posts-heading">{t.profile.theme}</h3>
        <div className="profile__theme-options" role="group" aria-label={t.profile.theme}>
          <button
            type="button"
            className={`profile__theme-btn ${theme === 'dark' ? 'profile__theme-btn--active' : ''}`}
            onClick={() => setTheme('dark')}
          >
            {t.profile.dark}
          </button>
          <button
            type="button"
            className={`profile__theme-btn ${theme === 'light' ? 'profile__theme-btn--active' : ''}`}
            onClick={() => setTheme('light')}
          >
            {t.profile.light}
          </button>
        </div>
      </div>

      <div className="profile__theme-card">
        <h3 className="profile__posts-heading">{t.profile.language}</h3>
        <div className="profile__theme-options" role="group" aria-label={t.profile.language}>
          <button
            type="button"
            className={`profile__theme-btn ${language === 'en' ? 'profile__theme-btn--active' : ''}`}
            onClick={() => setLanguage('en')}
          >
            EN
          </button>
          <button
            type="button"
            className={`profile__theme-btn ${language === 'ru' ? 'profile__theme-btn--active' : ''}`}
            onClick={() => setLanguage('ru')}
          >
            RU
          </button>
        </div>
      </div>

      {!contactLoading && !hasContactInfo ? (
        <div className="profile__contact-reminder">
          <div className="profile__contact-reminder-copy">
            <h3>{t.profile.contactReminderTitle}</h3>
            <p>{t.profile.contactReminderBody}</p>
          </div>
          <button type="button" className="profile__contact-reminder-btn" onClick={focusContactFields}>
            {t.profile.addContacts}
          </button>
        </div>
      ) : null}

      <div className="profile__contact-card" ref={contactCardRef}>
        <h3 className="profile__posts-heading">{t.contact.contactInfo}</h3>
        <p className="profile__contact-hint">
          {t.contact.contactHint}
        </p>

        {!initData ? (
          <p className="profile__state">{t.contact.contactEditingRequiresTelegram}</p>
        ) : (
          <>
            {contactLoading ? <p className="profile__state">{t.contact.loadingContactInfo}</p> : null}
            <form className="profile__contact-form" onSubmit={handleContactSubmit}>
              <label className="profile__contact-field">
                <span>{t.contact.telegramUsername}</span>
                <input
                  ref={telegramContactInputRef}
                  value={contactDraft.contactTelegramUsername}
                  onChange={(e) =>
                    setContactDraft((prev) => ({ ...prev, contactTelegramUsername: e.target.value }))
                  }
                  placeholder="username"
                  maxLength={32}
                />
              </label>
              <label className="profile__contact-field">
                <span>{t.contact.email}</span>
                <input
                  type="email"
                  value={contactDraft.contactEmail}
                  onChange={(e) => setContactDraft((prev) => ({ ...prev, contactEmail: e.target.value }))}
                  placeholder="you@example.com"
                  maxLength={255}
                />
              </label>
              <button type="submit" className="profile__contact-save" disabled={contactSaving}>
                {contactSaving ? t.contact.saving : t.contact.saveContactInfo}
              </button>
            </form>
            {contactMessage ? (
              <p className={`profile__state ${contactMessage === t.contact.contactInfoSaved ? '' : 'profile__state--error'}`}>
                {contactMessage}
              </p>
            ) : null}
            <div className="profile__contact-preview">
              <h4 className="profile__contact-preview-title">{t.contact.currentContactInfo}</h4>
              <ContactInfoView contact={contactInfo} emptyMessage={t.contact.noOwnContactInfo} />
            </div>
          </>
        )}
      </div>

      <div className="profile__feedback-card">
        <h3 className="profile__posts-heading">{t.profile.feedbackTitle}</h3>
        <a className="profile__feedback-link" href={DEVELOPER_CONTACT_URL} target="_blank" rel="noopener noreferrer">
          {t.profile.feedbackText}
        </a>
      </div>

      <div className="profile__posts">
        <h3 className="profile__posts-heading">{t.profile.myPosts}</h3>

        {loading ? <p className="profile__state">{t.common.loading}</p> : null}
        {!loading && error ? <p className="profile__state profile__state--error">{error}</p> : null}
        {!loading && !error && myPosts.length === 0 ? <p className="profile__state">{t.profile.noPosts}</p> : null}

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
        <h3 className="profile__posts-heading">{t.profile.myApplications}</h3>

        {applicationsLoading ? <p className="profile__state">{t.common.loading}</p> : null}
        {!applicationsLoading && applicationsError ? (
          <p className="profile__state profile__state--error">{applicationsError}</p>
        ) : null}
        {!applicationsLoading && !applicationsError && applications.length === 0 ? (
          <p className="profile__state">{t.profile.noApplications}</p>
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
                    <span>{getPostGoalLabel(t, application.postGoal)}</span>
                    <span className="profile__application-dot">·</span>
                    <span>{getPostStatusLabel(t, application.postStatus)}</span>
                  </p>
                </div>
                <span className={`profile__application-status profile__application-status--${application.status.toLowerCase()}`}>
                  {getApplicationStatusLabel(t, application.status)}
                </span>
              </div>
              <p className="profile__application-time">{formatWhen(application.createdAt)}</p>

              {application.contactAvailable ? (
                <div className="profile__application-contact">
                  <div className="profile__application-contact-copy">
                    <h5>{t.contact.acceptedApplicationTitle}</h5>
                    <p>
                      {unlockedContacts[application.id] && !hasUnlockedContactInfo(unlockedContacts[application.id])
                        ? t.contact.acceptedApplicationNoContact
                        : t.contact.acceptedApplicationIntro}
                    </p>
                  </div>
                  <button
                    type="button"
                    className="profile__application-contact-btn"
                    disabled={contactBusyId !== null}
                    onClick={() => void handleViewContact(application.id)}
                  >
                    {contactBusyId === application.id ? t.contact.loadingContact : t.contact.viewContact}
                  </button>
                  {contactErrorById[application.id] ? (
                    <p className="profile__application-contact-note profile__application-contact-note--error">
                      {contactErrorById[application.id]}
                    </p>
                  ) : null}
                  {unlockedContacts[application.id] && hasUnlockedContactInfo(unlockedContacts[application.id]) ? (
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
