import { useMemo, useState } from 'react';
import { BottomNav, type TabId } from '../components/BottomNav';
import { useTelegramEnvironment } from '../hooks/useTelegramEnvironment';
import { CreatePostPage } from './CreatePostPage';
import { FeedPage } from './FeedPage';
import { ProfilePage } from './ProfilePage';

const FALLBACK_TELEGRAM_ID = 123456789;

export function HomePage() {
  const [currentTab, setCurrentTab] = useState<TabId>('posts');
  const [feedReloadToken, setFeedReloadToken] = useState(0);

  const { user } = useTelegramEnvironment();

  const telegramId = useMemo(() => user?.id ?? FALLBACK_TELEGRAM_ID, [user?.id]);
  const username = user?.username;

  function handleCreated() {
    setFeedReloadToken((x) => x + 1);
    setCurrentTab('posts');
  }

  return (
    <>
      {currentTab === 'posts' ? <FeedPage reloadToken={feedReloadToken} /> : null}
      {currentTab === 'create' ? <CreatePostPage telegramId={telegramId} onCreated={handleCreated} /> : null}
      {currentTab === 'profile' ? <ProfilePage telegramId={telegramId} username={username} /> : null}

      <BottomNav currentTab={currentTab} onTabChange={setCurrentTab} />
    </>
  );
}
