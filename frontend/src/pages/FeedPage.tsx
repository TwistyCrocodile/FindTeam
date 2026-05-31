import { useCallback, useEffect, useMemo, useState } from 'react';
import { getApplicationsByApplicantAuthAware } from '../api/applications';
import { getPosts } from '../api/posts';
import { getFriendlyErrorMessage } from '../app/errors';
import { getPostGoalLabel, getPostStatusLabel, getPostTypeLabel } from '../app/translations';
import { PostCard } from '../components/PostCard';
import { useLanguage } from '../hooks/useLanguage';
import type { PostGoal, PostResponse, PostStatus, PostType } from '../types/post';
import './FeedPage.css';

const PAGE_SIZE = 10;

function normalizeQuery(q: string) {
  return q.trim().toLowerCase();
}

function postMatchesQuery(post: PostResponse, q: string) {
  if (!q) return true;
  const haystack = [post.title, post.description, post.stack].filter(Boolean).join(' ').toLowerCase();
  return haystack.includes(q);
}

type Props = {
  reloadToken: number;
  viewerTelegramId: number;
  initData?: string | null;
  onViewUserProfile?: (telegramId: number) => void;
};

export function FeedPage({ reloadToken, viewerTelegramId, initData, onViewUserProfile }: Props) {
  const { t } = useLanguage();
  const [search, setSearch] = useState('');

  const [filterType, setFilterType] = useState<PostType | ''>('');
  const [filterGoal, setFilterGoal] = useState<PostGoal | ''>('');
  // Product direction: default to ACTIVE-first when user hasn't explicitly changed it.
  const [filterStatus, setFilterStatus] = useState<PostStatus | ''>('ACTIVE');

  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [appliedPostIds, setAppliedPostIds] = useState<Set<number>>(() => new Set());

  useEffect(() => {
    let cancelled = false;
    async function loadMyApplications() {
      try {
        const list = await getApplicationsByApplicantAuthAware(viewerTelegramId, initData);
        if (cancelled) return;
        setAppliedPostIds(new Set(list.map((a) => a.postId)));
      } catch {
        if (!cancelled) setAppliedPostIds(new Set());
      }
    }
    void loadMyApplications();
    return () => {
      cancelled = true;
    };
  }, [viewerTelegramId, initData, reloadToken]);

  const filters = useMemo(
    () => ({
      type: filterType || undefined,
      goal: filterGoal || undefined,
      status: filterStatus || undefined,
    }),
    [filterType, filterGoal, filterStatus],
  );

  const refreshFirstPage = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getPosts(filters, 0, PAGE_SIZE);
      setPosts(res.content);
      setPage(0);
      setLast(res.last);
    } catch (e) {
      setError(getFriendlyErrorMessage(e, t.feed.failedToLoadPosts, t));
      setPosts([]);
      setLast(true);
    } finally {
      setLoading(false);
    }
  }, [filters, t.feed.failedToLoadPosts]);

  useEffect(() => {
    void refreshFirstPage();
  }, [refreshFirstPage, reloadToken]);

  const loadMore = useCallback(async () => {
    if (last || loadingMore || loading) return;
    const nextPage = page + 1;
    setLoadingMore(true);
    setError(null);
    try {
      const res = await getPosts(filters, nextPage, PAGE_SIZE);
      setPosts((prev) => [...prev, ...res.content]);
      setPage(nextPage);
      setLast(res.last);
    } catch (e) {
      setError(getFriendlyErrorMessage(e, t.feed.failedToLoadMore, t));
    } finally {
      setLoadingMore(false);
    }
  }, [filters, last, loadingMore, loading, page, t.feed.failedToLoadMore]);

  function handlePostUpdated(updated: PostResponse) {
    setPosts((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
  }

  function handlePostDeleted(postId: number) {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
  }

  const normalizedSearch = useMemo(() => normalizeQuery(search), [search]);
  const visiblePosts = useMemo(
    () => posts.filter((p) => postMatchesQuery(p, normalizedSearch)),
    [posts, normalizedSearch],
  );

  return (
    <section className="feed">
      <h2 className="feed__heading">{t.feed.posts}</h2>

      <label className="feed__search">
        <span className="feed__search-label">{t.feed.search}</span>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder={t.feed.searchPlaceholder}
          autoComplete="off"
        />
      </label>

      <div className="feed__filters">
        <label className="feed__filter">
          <span>{t.feed.type}</span>
          <select value={filterType} onChange={(e) => setFilterType(e.target.value as PostType | '')}>
            <option value="">{t.common.all}</option>
            <option value="SEEKING_TEAM">{getPostTypeLabel(t, 'SEEKING_TEAM')}</option>
            <option value="SEEKING_MEMBER">{getPostTypeLabel(t, 'SEEKING_MEMBER')}</option>
          </select>
        </label>
        <label className="feed__filter">
          <span>{t.feed.goal}</span>
          <select value={filterGoal} onChange={(e) => setFilterGoal(e.target.value as PostGoal | '')}>
            <option value="">{t.common.all}</option>
            <option value="HACKATHON">{getPostGoalLabel(t, 'HACKATHON')}</option>
            <option value="PET_PROJECT">{getPostGoalLabel(t, 'PET_PROJECT')}</option>
            <option value="STARTUP">{getPostGoalLabel(t, 'STARTUP')}</option>
            <option value="JOB">{getPostGoalLabel(t, 'JOB')}</option>
          </select>
        </label>
        <label className="feed__filter">
          <span>{t.feed.status}</span>
          <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value as PostStatus | '')}>
            <option value="">{t.common.all}</option>
            <option value="ACTIVE">{getPostStatusLabel(t, 'ACTIVE')}</option>
            <option value="CLOSED">{getPostStatusLabel(t, 'CLOSED')}</option>
          </select>
        </label>
      </div>

      {loading ? <p className="feed__state">{t.feed.loadingPosts}</p> : null}

      {!loading && error ? <p className="feed__state feed__state--error">{error}</p> : null}

      {!loading && !error && posts.length === 0 ? (
        <p className="feed__state">{t.feed.noPostsFound}</p>
      ) : null}

      {!loading && !error && posts.length > 0 && visiblePosts.length === 0 ? (
        <p className="feed__state">{t.feed.noSearchMatches}</p>
      ) : null}

      <ul className="feed__list">
        {visiblePosts.map((p) => (
          <li key={p.id}>
            <PostCard
              post={p}
              viewerTelegramId={viewerTelegramId}
              initData={initData}
              hasApplied={appliedPostIds.has(p.id)}
              onApplied={(postId) => setAppliedPostIds((prev) => new Set(prev).add(postId))}
              onViewUserProfile={onViewUserProfile}
              onPostUpdated={handlePostUpdated}
              onPostDeleted={handlePostDeleted}
            />
          </li>
        ))}
      </ul>

      {!loading && !last ? (
        <div className="feed__more">
          <button type="button" className="btn-load-more" disabled={loadingMore} onClick={() => void loadMore()}>
            {loadingMore ? t.feed.loadingMore : t.feed.loadMore}
          </button>
        </div>
      ) : null}
    </section>
  );
}

