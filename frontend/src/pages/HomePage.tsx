import { useEffect, useMemo, useState } from 'react';
import { BottomNav, type TabId } from '../components/BottomNav';
import { useLanguage } from '../hooks/useLanguage';
import { useTelegramEnvironment } from '../hooks/useTelegramEnvironment';
import { CreatePostPage } from './CreatePostPage';
import { FeedPage } from './FeedPage';
import { ProfilePage } from './ProfilePage';
import { PublicProfileView } from './PublicProfileView';
import { OnboardingPage } from './OnboardingPage';
import { getCurrentUserProfile, getUserProfile } from '../api/users';
import type { UserProfileResponse } from '../types/user';

const FALLBACK_TELEGRAM_ID = 123456789;

export function HomePage() {
  const { t } = useLanguage();
  const [currentTab, setCurrentTab] = useState<TabId>('posts');
  const [feedReloadToken, setFeedReloadToken] = useState(0);

  const { user, initData } = useTelegramEnvironment();

  const telegramId = useMemo(() => user?.id ?? FALLBACK_TELEGRAM_ID, [user?.id]);
  const nicknameSuggestion = user?.username ?? null;

  const [profileStatus, setProfileStatus] = useState<'loading' | 'onboarding' | 'ready'>('loading');
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [viewedUserTelegramId, setViewedUserTelegramId] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      setProfileStatus('loading');
      setProfileError(null);

      try {
        const p = initData ? await getCurrentUserProfile(initData) : await getUserProfile(telegramId);
        if (cancelled) return;
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

        setProfileError(err instanceof Error ? err.message : t.profile.failedToLoadProfile);
        setProfileStatus('onboarding');
      }
    }

    void loadProfile();
    return () => {
      cancelled = true;
    };
  }, [telegramId, initData, t.profile.failedToLoadProfile]);

  function handleCreated() {
    setFeedReloadToken((x) => x + 1);
    setCurrentTab('posts');
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

  return (
    <>
      {currentTab === 'posts' ? (
        <FeedPage
          reloadToken={feedReloadToken}
          viewerTelegramId={telegramId}
          initData={initData}
          onViewUserProfile={(id) => setViewedUserTelegramId(id)}
        />
      ) : null}
      {currentTab === 'create' ? (
        <CreatePostPage telegramId={telegramId} initData={initData} onCreated={handleCreated} />
      ) : null}
      {currentTab === 'profile' ? (
        <ProfilePage
          telegramId={telegramId}
          initData={initData}
          profile={profile}
          onProfileUpdated={(p) => setProfile(p)}
          onViewUserProfile={(id) => setViewedUserTelegramId(id)}
        />
      ) : null}

      <BottomNav currentTab={currentTab} onTabChange={setCurrentTab} />
    </>
  );
}
