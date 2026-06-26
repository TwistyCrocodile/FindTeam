import { useEffect, useState } from 'react';
import { applyToPostAuthAware } from '../api/applications';
import { closePostAuthAware, deletePostAuthAware, reopenPostAuthAware } from '../api/posts';
import { getFriendlyErrorMessage } from '../app/errors';
import { getPostGoalLabel, getPostStatusLabel, getPostTypeLabel, getUserStatusLabel } from '../app/translations';
import { useLanguage } from '../hooks/useLanguage';
import type { PostResponse } from '../types/post';
import { CreatePostForm } from './CreatePostForm';
import { MarkdownText } from './MarkdownText';
import './PostCard.css';

type Props = {
  post: PostResponse;
  viewerTelegramId: number;
  initData?: string | null;
  onPostUpdated: (p: PostResponse) => void;
  onPostDeleted: (postId: number) => void;
  /** True if the viewer already applied to this post (from feed preload). */
  hasApplied?: boolean;
  viewerHasContactInfo?: boolean;
  onAddContacts?: () => void;
  onApplied?: (postId: number) => void;
  /** Profile owner view: show View Applications. */
  showApplicationsButton?: boolean;
  onViewApplications?: (postId: number) => void;
  onViewUserProfile?: (authorTelegramId: number) => void;
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
  initData,
  onPostUpdated,
  onPostDeleted,
  hasApplied = false,
  viewerHasContactInfo = true,
  onAddContacts,
  onApplied,
  showApplicationsButton = false,
  onViewApplications,
  onViewUserProfile,
}: Props) {
  const { t } = useLanguage();
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [applyNotice, setApplyNotice] = useState<string | null>(null);
  const [applied, setApplied] = useState(hasApplied);
  const [showMissingContactDialog, setShowMissingContactDialog] = useState(false);
  const [showOwnerMenu, setShowOwnerMenu] = useState(false);
  const [editingPost, setEditingPost] = useState(false);
  const [showDeleteConfirmDialog, setShowDeleteConfirmDialog] = useState(false);

  useEffect(() => {
    setApplied(hasApplied);
  }, [hasApplied]);

  const isOwner = post.telegramId === viewerTelegramId;
  const alreadyApplied = applied || hasApplied;
  const isClosed = post.status === 'CLOSED';

  if (editingPost) {
    return (
      <CreatePostForm
        mode="edit"
        post={post}
        telegramId={viewerTelegramId}
        initData={initData}
        onUpdated={(updated) => {
          onPostUpdated(updated);
          setEditingPost(false);
          setApplyNotice(t.postActions.postUpdated);
        }}
        onCancel={() => setEditingPost(false)}
      />
    );
  }

  async function submitApplication() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      await applyToPostAuthAware({ postId: post.id, telegramId: viewerTelegramId }, initData);
      setApplied(true);
      onApplied?.(post.id);
      setApplyNotice(t.applications.applicationSent);
    } catch (e) {
      const msg = getFriendlyErrorMessage(e, t.applications.couldNotApply, t);
      if (msg.toLowerCase().includes('already exists')) {
        setApplied(true);
        onApplied?.(post.id);
        setApplyNotice(t.applications.alreadyApplied);
      } else {
        setActionError(msg);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleApply() {
    if (!viewerHasContactInfo) {
      setShowMissingContactDialog(true);
      return;
    }
    await submitApplication();
  }

  async function handleClose() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      const updated = await closePostAuthAware(post.id, viewerTelegramId, initData);
      onPostUpdated(updated);
    } catch (e) {
      setActionError(getFriendlyErrorMessage(e, t.postActions.couldNotClose, t));
    } finally {
      setBusy(false);
    }
  }

  async function handleReopen() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      const updated = await reopenPostAuthAware(post.id, viewerTelegramId, initData);
      onPostUpdated(updated);
    } catch (e) {
      setActionError(getFriendlyErrorMessage(e, t.postActions.couldNotReopen, t));
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete() {
    setActionError(null);
    setApplyNotice(null);
    setBusy(true);
    try {
      await deletePostAuthAware(post.id, viewerTelegramId, initData);
      onPostDeleted(post.id);
    } catch (e) {
      console.error('[FindTeam] Delete post failed', {
        postId: post.id,
        viewerTelegramId,
        hasInitData: Boolean(initData),
        error: e,
      });
      setActionError(getFriendlyErrorMessage(e, t.postActions.couldNotDelete, t));
    } finally {
      setBusy(false);
    }
  }

  return (
    <article className="post-card">
      <div className="post-card__head">
        <h2 className="post-card__title">{post.title}</h2>
        <div className="post-card__badges">
          {post.language ? <span className="post-card__language">{post.language}</span> : null}
          <span className={`post-card__status post-card__status--${post.status.toLowerCase()}`}>
            {getPostStatusLabel(t, post.status)}
          </span>
        </div>
      </div>
      <p className="post-card__meta">
        {onViewUserProfile && post.telegramId && post.telegramId !== viewerTelegramId ? (
          <button
            type="button"
            className="post-card__author"
            onClick={() => onViewUserProfile(post.telegramId)}
          >
            {post.nickname ?? t.common.unknownUser}
          </button>
        ) : (
          <strong>{post.nickname ?? t.common.unknownUser}</strong>
        )}
        <span className="post-card__dot">·</span>
        <span className="post-card__meta-pill">{getUserStatusLabel(t, post.authorStatus)}</span>
        <span className="post-card__dot">·</span>
        <span className="post-card__meta-pill">{getPostTypeLabel(t, post.type)}</span>
        <span className="post-card__dot">·</span>
        <span className="post-card__meta-pill">{getPostGoalLabel(t, post.goal)}</span>
      </p>
      <p className="post-card__stack">
        <span className="post-card__label">{t.createPost.stack}</span> {post.stack}
      </p>
      <MarkdownText className="post-card__description">{post.description}</MarkdownText>
      {post.eventLink ? (
        <p className="post-card__link">
          <span className="post-card__label">{t.common.link}</span>{' '}
          <a href={post.eventLink} target="_blank" rel="noopener noreferrer">
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
            {busy
              ? t.applications.applying
              : isClosed
                ? t.postActions.closedButton
                : alreadyApplied
                  ? t.applications.applied
                  : t.applications.apply}
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
                {t.applications.viewApplications}
              </button>
            ) : null}
            <div className="post-card__menu-wrap">
              <button
                type="button"
                className="btn btn--secondary post-card__menu-trigger"
                aria-label={t.postActions.actions}
                aria-expanded={showOwnerMenu}
                disabled={busy}
                onClick={() => setShowOwnerMenu((open) => !open)}
              >
                ⋮
              </button>
              {showOwnerMenu ? (
                <div className="post-card__menu" role="menu">
                  <button
                    type="button"
                    className="post-card__menu-item"
                    role="menuitem"
                    onClick={() => {
                      setShowOwnerMenu(false);
                      setEditingPost(true);
                    }}
                  >
                    ✏️ {t.postActions.editPost}
                  </button>
                  {!isClosed ? (
                    <button
                      type="button"
                      className="post-card__menu-item"
                      role="menuitem"
                      onClick={() => {
                        setShowOwnerMenu(false);
                        void handleClose();
                      }}
                    >
                      🔒 {t.postActions.closePost}
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="post-card__menu-item"
                      role="menuitem"
                      onClick={() => {
                        setShowOwnerMenu(false);
                        void handleReopen();
                      }}
                    >
                      🔓 {t.postActions.reopenPost}
                    </button>
                  )}
                  <button
                    type="button"
                    className="post-card__menu-item post-card__menu-item--danger"
                    role="menuitem"
                    onClick={() => {
                      setShowOwnerMenu(false);
                      setShowDeleteConfirmDialog(true);
                    }}
                  >
                    🗑 {t.postActions.deletePost}
                  </button>
                </div>
              ) : null}
            </div>
          </>
        ) : null}
      </div>
      {applyNotice ? <p className="post-card__apply-notice post-card__apply-notice--ok">{applyNotice}</p> : null}
      {actionError ? <p className="post-card__error">{actionError}</p> : null}
      {showDeleteConfirmDialog ? (
        <div className="post-card__dialog-backdrop" role="presentation">
          <div
            className="post-card__dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby={`delete-post-title-${post.id}`}
          >
            <h3 id={`delete-post-title-${post.id}`} className="post-card__dialog-title">
              {t.postActions.deleteConfirmTitle}
            </h3>
            <p className="post-card__dialog-copy">{t.postActions.deleteConfirmBody}</p>
            <div className="post-card__dialog-actions">
              <button
                type="button"
                className="btn btn--secondary"
                disabled={busy}
                onClick={() => setShowDeleteConfirmDialog(false)}
              >
                {t.postActions.deleteConfirmCancel}
              </button>
              <button
                type="button"
                className="btn btn--primary post-card__delete-confirm"
                disabled={busy}
                onClick={() => {
                  setShowDeleteConfirmDialog(false);
                  void handleDelete();
                }}
              >
                {busy ? t.createPost.submitting : t.postActions.deleteConfirmDelete}
              </button>
            </div>
          </div>
        </div>
      ) : null}
      {showMissingContactDialog ? (
        <div className="post-card__dialog-backdrop" role="presentation">
          <div
            className="post-card__dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby={`missing-contact-title-${post.id}`}
          >
            <h3 id={`missing-contact-title-${post.id}`} className="post-card__dialog-title">
              {t.applications.missingContactTitle}
            </h3>
            <p className="post-card__dialog-copy">{t.applications.missingContactBody}</p>
            <div className="post-card__dialog-actions">
              <button
                type="button"
                className="btn btn--primary"
                onClick={() => {
                  setShowMissingContactDialog(false);
                  onAddContacts?.();
                }}
              >
                {t.applications.addContacts}
              </button>
              <button
                type="button"
                className="btn btn--secondary"
                disabled={busy}
                onClick={() => {
                  setShowMissingContactDialog(false);
                  void submitApplication();
                }}
              >
                {t.applications.continueAnyway}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </article>
  );
}
