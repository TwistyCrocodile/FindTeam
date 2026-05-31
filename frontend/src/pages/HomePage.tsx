import { useEffect, useMemo, useState } from 'react';
import { BottomNav, type TabId } from '../components/BottomNav';
import { useTelegramEnvironment } from '../hooks/useTelegramEnvironment';
import { CreatePostPage } from './CreatePostPage';
import { FeedPage } from './FeedPage';
import { ProfilePage } from './ProfilePage';
import { OnboardingPage } from './OnboardingPage';
import { getUserProfile } from '../api/users';
import type { UserProfileResponse } from '../types/user';

const FALLBACK_TELEGRAM_ID = 123456789;

export function HomePage() {
  const [currentTab, setCurrentTab] = useState<TabId>('posts');
  const [feedReloadToken, setFeedReloadToken] = useState(0);

  const { user } = useTelegramEnvironment();

  const telegramId = useMemo(() => user?.id ?? FALLBACK_TELEGRAM_ID, [user?.id]);
  const nicknameSuggestion = user?.username ?? null;

  const [profileStatus, setProfileStatus] = useState<'loading' | 'onboarding' | 'ready'>('loading');
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadProfile() {
      setProfileStatus('loading');
      setProfileError(null);

      try {
        const p = await getUserProfile(telegramId);
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

        setProfileError(err instanceof Error ? err.message : 'Failed to load profile');
        setProfileStatus('onboarding');
      }
    }

    void loadProfile();
    return () => {
      cancelled = true;
    };
  }, [telegramId]);

  function handleCreated() {
    setFeedReloadToken((x) => x + 1);
    setCurrentTab('posts');
  }

  if (profileStatus === 'loading') {
    return <p>Loading profile…</p>;
  }

  if (profileStatus === 'onboarding') {
    return (
      <OnboardingPage
        telegramId={telegramId}
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
      <p>{profileError ?? 'Profile not loaded.'}</p>
    );
  }

  return (
    <>
      {currentTab === 'posts' ? (
        <FeedPage reloadToken={feedReloadToken} viewerTelegramId={telegramId} />
      ) : null}
      {currentTab === 'create' ? <CreatePostPage telegramId={telegramId} onCreated={handleCreated} /> : null}
      {currentTab === 'profile' ? (
        <ProfilePage
          telegramId={telegramId}
          profile={profile}
          onProfileUpdated={(p) => setProfile(p)}
        />
      ) : null}

      <BottomNav currentTab={currentTab} onTabChange={setCurrentTab} />
    </>
  );
}
