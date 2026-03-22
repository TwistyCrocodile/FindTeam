import { useCallback, useEffect, useMemo, useState } from 'react';
import { getPosts } from '../api/posts';
import { CreatePostForm } from '../components/CreatePostForm';
import { PostCard } from '../components/PostCard';
import type { PostGoal, PostResponse, PostStatus, PostType } from '../types/post';
import './HomePage.css';

const PAGE_SIZE = 10;

export function HomePage() {
  const [filterType, setFilterType] = useState<PostType | ''>('');
  const [filterGoal, setFilterGoal] = useState<PostGoal | ''>('');
  const [filterStatus, setFilterStatus] = useState<PostStatus | ''>('');

  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
  }, [refreshFirstPage]);

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

  return (
    <>
      <CreatePostForm onCreated={() => void refreshFirstPage()} />

      <section className="feed">
        <h2 className="feed__heading">Feed</h2>

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
          <p className="feed__state">No posts yet. Create one above or adjust filters.</p>
        ) : null}

        <ul className="feed__list">
          {posts.map((p) => (
            <li key={p.id}>
              <PostCard post={p} onPostUpdated={handlePostUpdated} />
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
    </>
  );
}
