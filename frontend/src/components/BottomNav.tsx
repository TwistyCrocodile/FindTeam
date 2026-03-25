import './BottomNav.css';

export type TabId = 'posts' | 'create' | 'profile';

type Props = {
  currentTab: TabId;
  onTabChange: (tab: TabId) => void;
};

export function BottomNav({ currentTab, onTabChange }: Props) {
  return (
    <nav className="bottom-nav" aria-label="Bottom navigation">
      <button
        type="button"
        className={`bottom-nav__item ${currentTab === 'posts' ? 'bottom-nav__item--active' : ''}`}
        onClick={() => onTabChange('posts')}
      >
        Posts
      </button>
      <button
        type="button"
        className={`bottom-nav__item ${currentTab === 'create' ? 'bottom-nav__item--active' : ''}`}
        onClick={() => onTabChange('create')}
      >
        Create
      </button>
      <button
        type="button"
        className={`bottom-nav__item ${currentTab === 'profile' ? 'bottom-nav__item--active' : ''}`}
        onClick={() => onTabChange('profile')}
      >
        Profile
      </button>
    </nav>
  );
}
