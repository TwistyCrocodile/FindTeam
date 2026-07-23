import { useEffect, useMemo, useState } from 'react';
import { getFriendlyErrorMessage } from '../app/errors';
import { BottomNav, type TabId } from '../components/BottomNav';
import { initializeLanguageFromPreferredLanguage, useLanguage } from '../hooks/useLanguage';
import { useTelegramEnvironment } from '../hooks/useTelegramEnvironment';
import { CreatePostPage } from './CreatePostPage';
import { FeedPage } from './FeedPage';
import { ProfilePage } from './ProfilePage';
import { PublicProfileView } from './PublicProfileView';
import { OnboardingPage } from './OnboardingPage';
import { getCurrentUserProfile, getMyContactInfo, getUserProfile } from '../api/users';
import { hasAnyContact } from '../utils/contactInfo';
import type { ContactInfoResponse, UserProfileResponse } from '../types/user';

const FALLBACK_TELEGRAM_ID = 123456789;

export function HomePage() {
  const { t } = useLanguage();
  const [currentTab, setCurrentTab] = useState<TabId>('posts');
  const [feedReloadToken, setFeedReloadToken] = useState(0);

  const { user, initData } = useTelegramEnvironment();

  const telegramId = useMemo(() => user?.id ?? FALLBACK_TELEGRAM_ID, [user?.id]);
  const nicknameSuggestion = user?.username ?? user?.firstName ?? null;

  const [profileStatus, setProfileStatus] = useState<'loading' | 'onboarding' | 'ready'>('loading');
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [contactInfo, setContactInfo] = useState<ContactInfoResponse | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [viewedUserTelegramId, setViewedUserTelegramId] = useState<number | null>(null);
  const [contactFocusToken, setContactFocusToken] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      setProfileStatus('loading');
      setProfileError(null);

      try {
        const p = initData ? await getCurrentUserProfile(initData) : await getUserProfile(telegramId);
        if (cancelled) return;
        initializeLanguageFromPreferredLanguage(p.preferredLanguage);
        setProfile(p);
        setProfileStatus('ready');
      } catch (e) {
        const err = e as Error & { status?: number };
        if (cancelled) return;
        if (err.status === 404) {
          setProfile(null);
          setProfileStatus('onboarding');
          return;
        }

        setProfileError(getFriendlyErrorMessage(e, t.profile.failedToLoadProfile, t));
        setProfileStatus('onboarding');
      }
    }

    void loadProfile();
    return () => {
      cancelled = true;
    };
  }, [telegramId, initData, t.profile.failedToLoadProfile]);

  useEffect(() => {
    if (!initData || profileStatus !== 'ready') {
      setContactInfo(null);
      return;
    }

    let cancelled = false;
    async function loadContactInfo() {
      try {
        const info = await getMyContactInfo(initData as string);
        if (!cancelled) setContactInfo(info);
      } catch {
        if (!cancelled) setContactInfo(null);
      }
    }

    void loadContactInfo();
    return () => {
      cancelled = true;
    };
  }, [initData, profileStatus]);

  function handleCreated() {
    setFeedReloadToken((x) => x + 1);
    setCurrentTab('posts');
  }

  function handleAddContacts() {
    setCurrentTab('profile');
    setContactFocusToken((x) => x + 1);
  }

  if (profileStatus === 'loading') {
    return <p>{t.common.loadingProfile}</p>;
  }

  if (profileStatus === 'onboarding') {
    return (
      <OnboardingPage
        telegramId={telegramId}
        initData={initData}
        nicknameSuggestion={nicknameSuggestion}
        onCreated={(p) => {
          setProfile(p);
          setProfileStatus('ready');
          setCurrentTab('posts');
        }}
      />
    );
  }

  if (!profile) {
    return (
      <p>{profileError ?? t.profile.profileNotLoaded}</p>
    );
  }

  if (viewedUserTelegramId !== null) {
    return (
      <PublicProfileView
        telegramId={viewedUserTelegramId}
        onBack={() => setViewedUserTelegramId(null)}
      />
    );
  }

  const viewerTelegramId = profile.telegramId;

  return (
    <>
      {currentTab === 'posts' ? (
        <FeedPage
          reloadToken={feedReloadToken}
          viewerTelegramId={viewerTelegramId}
          initData={initData}
          viewerHasContactInfo={hasAnyContact(profile, contactInfo)}
          onAddContacts={handleAddContacts}
          onViewUserProfile={(id) => setViewedUserTelegramId(id)}
        />
      ) : null}
      {currentTab === 'create' ? (
        <CreatePostPage telegramId={viewerTelegramId} initData={initData} onCreated={handleCreated} />
      ) : null}
      {currentTab === 'profile' ? (
        <ProfilePage
          telegramId={viewerTelegramId}
          initData={initData}
          profile={profile}
          onProfileUpdated={(p) => setProfile(p)}
          contactInfoSnapshot={contactInfo}
          onContactInfoUpdated={setContactInfo}
          contactFocusToken={contactFocusToken}
          onViewUserProfile={(id) => setViewedUserTelegramId(id)}
        />
      ) : null}

      <BottomNav currentTab={currentTab} onTabChange={setCurrentTab} />
    </>
  );
}
