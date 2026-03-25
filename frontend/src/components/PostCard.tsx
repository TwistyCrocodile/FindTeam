import { useState } from 'react';
import { closePost, reopenPost } from '../api/posts';
import type { PostResponse } from '../types/post';
import './PostCard.css';

type Props = {
  post: PostResponse;
  onPostUpdated: (p: PostResponse) => void;
};

function formatWhen(iso: string) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

export function PostCard({ post, onPostUpdated }: Props) {
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  function handleApply() {
    // TODO: later open Telegram chat with author
    // For now, keep it simple.
    alert(`Applied to "${post.title}"`);
    console.log('Apply clicked', { postId: post.id, telegramId: post.telegramId });
  }

  async function handleClose() {
    setActionError(null);
    setBusy(true);
    try {
      const updated = await closePost(post.id);
      onPostUpdated(updated);
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not close post');
    } finally {
      setBusy(false);
    }
  }

  async function handleReopen() {
    setActionError(null);
    setBusy(true);
    try {
      const updated = await reopenPost(post.id);
      onPostUpdated(updated);
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not reopen post');
    } finally {
      setBusy(false);
    }
  }

  return (
    <article className="post-card">
      <div className="post-card__head">
        <h2 className="post-card__title">{post.title}</h2>
        <span className={`post-card__status post-card__status--${post.status.toLowerCase()}`}>
          {post.status}
        </span>
      </div>
      <p className="post-card__meta">
        <strong>{post.username ?? 'Unknown user'}</strong>
        <span className="post-card__dot">·</span>
        <span>{post.type}</span>
        <span className="post-card__dot">·</span>
        <span>{post.goal}</span>
      </p>
      <p className="post-card__stack">
        <span className="post-card__label">Stack</span> {post.stack}
      </p>
      <p className="post-card__description">{post.description}</p>
      {post.eventLink ? (
        <p className="post-card__link">
          <span className="post-card__label">Link</span>{' '}
          <a href={post.eventLink} target="_blank" rel="noreferrer">
            {post.eventLink}
          </a>
        </p>
      ) : null}
      <p className="post-card__time">{formatWhen(post.createdAt)}</p>

      <div className="post-card__actions">
        <button type="button" className="btn btn--primary" disabled={busy} onClick={handleApply}>
          Apply
        </button>
        <button type="button" className="btn btn--secondary" disabled={busy} onClick={handleClose}>
          Close post
        </button>
        <button type="button" className="btn btn--secondary" disabled={busy} onClick={handleReopen}>
          Reopen post
        </button>
      </div>
      {actionError ? <p className="post-card__error">{actionError}</p> : null}
    </article>
  );
}
