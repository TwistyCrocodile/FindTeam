import './BottomNav.css';
import { useLanguage } from '../hooks/useLanguage';

export type TabId = 'posts' | 'create' | 'profile';

type Props = {
  currentTab: TabId;
  onTabChange: (tab: TabId) => void;
};

export function BottomNav({ currentTab, onTabChange }: Props) {
  const { t } = useLanguage();

  return (
    <nav className="bottom-nav" aria-label="Bottom navigation">
      <button
        type="button"
        className={`bottom-nav__item ${currentTab === 'posts' ? 'bottom-nav__item--active' : ''}`}
        onClick={() => onTabChange('posts')}
      >
        {t.nav.posts}
      </button>
      <button
        type="button"
        className={`bottom-nav__item ${currentTab === 'create' ? 'bottom-nav__item--active' : ''}`}
        onClick={() => onTabChange('create')}
      >
        {t.nav.create}
      </button>
      <button
        type="button"
        className={`bottom-nav__item ${currentTab === 'profile' ? 'bottom-nav__item--active' : ''}`}
        onClick={() => onTabChange('profile')}
      >
        {t.nav.profile}
      </button>
    </nav>
  );
}
