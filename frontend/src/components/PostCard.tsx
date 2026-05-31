import { useEffect, useState } from 'react';
import { applyToPost } from '../api/applications';
import { closePost, deletePost, reopenPost } from '../api/posts';
import type { PostResponse } from '../types/post';
import './PostCard.css';

type Props = {
  post: PostResponse;
  viewerTelegramId: number;
  onPostUpdated: (p: PostResponse) => void;
  onPostDeleted: (postId: number) => void;
  /** True if the viewer already applied to this post (from feed preload). */
  hasApplied?: boolean;
  onApplied?: (postId: number) => void;
  /** Profile owner view: show View Applications. */
  showApplicationsButton?: boolean;
  onViewApplications?: (postId: number) => void;
};

function formatWhen(iso: string) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

export function PostCard({
  post,
  viewerTelegramId,
  onPostUpdated,
  onPostDeleted,
  hasApplied = false,
  onApplied,
  showApplicationsButton = false,
  onViewApplications,
}: Props) {
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [applyNotice, setApplyNotice] = useState<string | null>(null);
  const [applied, setApplied] = useState(hasApplied);

  useEffect(() => {
    setApplied(hasApplied);
  }, [hasApplied]);

  const isOwner = post.telegramId === viewerTelegramId;
  const alreadyApplied = applied || hasApplied;
  const isClosed = post.status === 'CLOSED';

  async function handleApply() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      await applyToPost({ postId: post.id, telegramId: viewerTelegramId });
      setApplied(true);
      onApplied?.(post.id);
      setApplyNotice('Application sent');
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Could not apply';
      if (msg.toLowerCase().includes('already exists')) {
        setApplied(true);
        onApplied?.(post.id);
        setApplyNotice('You have already applied to this post.');
      } else {
        setActionError(msg);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleClose() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      const updated = await closePost(post.id, viewerTelegramId);
      onPostUpdated(updated);
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not close post');
    } finally {
      setBusy(false);
    }
  }

  async function handleReopen() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      const updated = await reopenPost(post.id, viewerTelegramId);
      onPostUpdated(updated);
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not reopen post');
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      await deletePost(post.id, viewerTelegramId);
      onPostDeleted(post.id);
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not delete post');
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
        <strong>{post.nickname ?? 'Unknown user'}</strong>
        <span className="post-card__dot">·</span>
        <span className="post-card__meta-pill">{post.type}</span>
        <span className="post-card__dot">·</span>
        <span className="post-card__meta-pill">{post.goal}</span>
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
        {!isOwner ? (
          <button
            type="button"
            className="btn btn--primary"
            disabled={busy || alreadyApplied || isClosed}
            onClick={() => void handleApply()}
          >
            {busy ? 'Sending…' : isClosed ? 'Closed' : alreadyApplied ? 'Applied' : 'Apply'}
          </button>
        ) : null}
        {isOwner ? (
          <>
            {showApplicationsButton && onViewApplications ? (
              <button
                type="button"
                className="btn btn--secondary"
                disabled={busy}
                onClick={() => onViewApplications(post.id)}
              >
                View Applications
              </button>
            ) : null}
            <button type="button" className="btn btn--secondary" disabled={busy} onClick={() => void handleClose()}>
              Close post
            </button>
            <button type="button" className="btn btn--secondary" disabled={busy} onClick={() => void handleReopen()}>
              Reopen post
            </button>
            <button type="button" className="btn btn--secondary" disabled={busy} onClick={() => void handleDelete()}>
              Delete post
            </button>
          </>
        ) : null}
      </div>
      {applyNotice ? <p className="post-card__apply-notice post-card__apply-notice--ok">{applyNotice}</p> : null}
      {actionError ? <p className="post-card__error">{actionError}</p> : null}
    </article>
  );
}
