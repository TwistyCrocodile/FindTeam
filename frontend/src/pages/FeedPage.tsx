import { useCallback, useEffect, useMemo, useState } from 'react';
import { getApplicationsByApplicantAuthAware } from '../api/applications';
import { getPosts } from '../api/posts';
import { PostCard } from '../components/PostCard';
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
};

export function FeedPage({ reloadToken, viewerTelegramId, initData }: Props) {
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
      setError(e instanceof Error ? e.message : 'Failed to load posts');
      setPosts([]);
      setLast(true);
    } finally {
      setLoading(false);
    }
  }, [filters]);

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
      setError(e instanceof Error ? e.message : 'Failed to load more');
    } finally {
      setLoadingMore(false);
    }
  }, [filters, last, loadingMore, loading, page]);

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
      <h2 className="feed__heading">Feed</h2>

      <label className="feed__search">
        <span className="feed__search-label">Search</span>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Title, description, stack…"
          autoComplete="off"
        />
      </label>

      <div className="feed__filters">
        <label className="feed__filter">
          <span>Type</span>
          <select value={filterType} onChange={(e) => setFilterType(e.target.value as PostType | '')}>
            <option value="">All</option>
            <option value="SEEKING_TEAM">SEEKING_TEAM</option>
            <option value="SEEKING_MEMBER">SEEKING_MEMBER</option>
          </select>
        </label>
        <label className="feed__filter">
          <span>Goal</span>
          <select value={filterGoal} onChange={(e) => setFilterGoal(e.target.value as PostGoal | '')}>
            <option value="">All</option>
            <option value="HACKATHON">HACKATHON</option>
            <option value="PET_PROJECT">PET_PROJECT</option>
            <option value="STARTUP">STARTUP</option>
            <option value="JOB">JOB</option>
          </select>
        </label>
        <label className="feed__filter">
          <span>Status</span>
          <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value as PostStatus | '')}>
            <option value="">All</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="CLOSED">CLOSED</option>
          </select>
        </label>
      </div>

      {loading ? <p className="feed__state">Loading posts…</p> : null}

      {!loading && error ? <p className="feed__state feed__state--error">{error}</p> : null}

      {!loading && !error && posts.length === 0 ? (
        <p className="feed__state">No posts yet. Create one or adjust filters.</p>
      ) : null}

      {!loading && !error && posts.length > 0 && visiblePosts.length === 0 ? (
        <p className="feed__state">No matches for your search.</p>
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
              onPostUpdated={handlePostUpdated}
              onPostDeleted={handlePostDeleted}
            />
          </li>
        ))}
      </ul>

      {!loading && !last ? (
        <div className="feed__more">
          <button type="button" className="btn-load-more" disabled={loadingMore} onClick={() => void loadMore()}>
            {loadingMore ? 'Loading…' : 'Load more'}
          </button>
        </div>
      ) : null}
    </section>
  );
}

