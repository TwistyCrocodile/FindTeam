import { FormEvent, useEffect, useMemo, useState } from 'react';
import { createPostAuthAware, updatePostAuthAware } from '../api/posts';
import { getFriendlyErrorMessage } from '../app/errors';
import { getPostGoalLabel, getPostTypeLabel } from '../app/translations';
import { useLanguage } from '../hooks/useLanguage';
import type { CreateCurrentUserPostRequest, CreatePostRequest, PostGoal, PostResponse, PostType } from '../types/post';
import './CreatePostForm.css';

const POST_TYPES: PostType[] = ['SEEKING_TEAM', 'SEEKING_MEMBER'];
const POST_GOALS: PostGoal[] = ['HACKATHON', 'PET_PROJECT', 'STARTUP', 'JOB', 'OLYMPIAD'];

type Props = {
  telegramId: number;
  initData?: string | null;
  mode?: 'create' | 'edit';
  post?: PostResponse;
  onCreated?: () => void;
  onUpdated?: (post: PostResponse) => void;
  onCancel?: () => void;
};

export function CreatePostForm({
  telegramId,
  initData,
  mode = 'create',
  post,
  onCreated,
  onUpdated,
  onCancel,
}: Props) {
  const { t } = useLanguage();
  const isEdit = mode === 'edit';
  const initialValues = useMemo(
    () => ({
      type: post?.type ?? 'SEEKING_TEAM',
      title: post?.title ?? '',
      description: post?.description ?? '',
      stack: post?.stack ?? '',
      goal: post?.goal ?? 'HACKATHON',
      eventLink: post?.eventLink ?? '',
    }),
    [post],
  );
  const [type, setType] = useState<PostType>(initialValues.type);
  const [title, setTitle] = useState(initialValues.title);
  const [description, setDescription] = useState(initialValues.description);
  const [stack, setStack] = useState(initialValues.stack);
  const [goal, setGoal] = useState<PostGoal>(initialValues.goal);
  const [eventLink, setEventLink] = useState(initialValues.eventLink);

  const [clientError, setClientError] = useState<string | null>(null);
  const [serverMessage, setServerMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setType(initialValues.type);
    setTitle(initialValues.title);
    setDescription(initialValues.description);
    setStack(initialValues.stack);
    setGoal(initialValues.goal);
    setEventLink(initialValues.eventLink);
    setClientError(null);
    setServerMessage(null);
  }, [initialValues]);

  function validate(): string | null {
    if (!Number.isFinite(telegramId) || telegramId <= 0) return t.createPost.telegramIdInvalid;
    if (!title.trim()) return t.createPost.titleRequired;
    if (!description.trim()) return t.createPost.descriptionRequired;
    if (!stack.trim()) return t.createPost.stackRequired;
    return null;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setClientError(null);
    setServerMessage(null);

    const v = validate();
    if (v) {
      setClientError(v);
      return;
    }

    const editablePayload: CreateCurrentUserPostRequest = {
      type,
      title: title.trim(),
      description: description.trim(),
      stack: stack.trim(),
      goal,
      eventLink: eventLink.trim() || undefined,
    };

    setSubmitting(true);
    try {
      if (isEdit && post) {
        const updated = await updatePostAuthAware(post.id, telegramId, editablePayload, initData);
        setServerMessage(t.createPost.updated);
        onUpdated?.(updated);
      } else {
        const payload: CreatePostRequest = {
          telegramId,
          ...editablePayload,
        };
        await createPostAuthAware(payload, initData);
        setServerMessage(t.createPost.created);
        setTitle('');
        setDescription('');
        setStack('');
        setEventLink('');
        onCreated?.();
      }
    } catch (err) {
      setServerMessage(getFriendlyErrorMessage(err, t.common.requestFailed, t));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="create-post">
      <h2 className="create-post__heading">{isEdit ? t.createPost.editHeading : t.createPost.heading}</h2>
      <p className="create-post__hint">{t.createPost.postingAs}</p>
      <form className="create-post__form" onSubmit={onSubmit}>
        <label className="field">
          <span>{t.createPost.type}</span>
          <select value={type} onChange={(e) => setType(e.target.value as PostType)}>
            {POST_TYPES.map((postType) => (
              <option key={postType} value={postType}>
                {getPostTypeLabel(t, postType)}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>{t.createPost.title}</span>
          <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={100} />
        </label>

        <label className="field">
          <span>{t.createPost.description}</span>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} maxLength={1000} />
          <span className="field__helper">{t.createPost.markdownHelp}</span>
        </label>

        <label className="field">
          <span>{t.createPost.stack}</span>
          <input value={stack} onChange={(e) => setStack(e.target.value)} maxLength={255} />
        </label>

        <label className="field">
          <span>{t.createPost.goal}</span>
          <select value={goal} onChange={(e) => setGoal(e.target.value as PostGoal)}>
            {POST_GOALS.map((g) => (
              <option key={g} value={g}>
                {getPostGoalLabel(t, g)}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>{t.createPost.eventLink}</span>
          <input
            value={eventLink}
            onChange={(e) => setEventLink(e.target.value)}
            placeholder="https://..."
            maxLength={255}
          />
        </label>

        {clientError ? <p className="form-msg form-msg--error">{clientError}</p> : null}
        {serverMessage ? (
          <p className={`form-msg ${serverMessage === t.createPost.created || serverMessage === t.createPost.updated ? 'form-msg--ok' : 'form-msg--error'}`}>
            {serverMessage}
          </p>
        ) : null}

        <div className="create-post__actions">
          <button type="submit" className="btn btn--primary" disabled={submitting}>
            {submitting ? t.createPost.submitting : isEdit ? t.common.saveChanges : t.createPost.submit}
          </button>
          {isEdit && onCancel ? (
            <button type="button" className="btn btn--secondary" disabled={submitting} onClick={onCancel}>
              {t.common.cancel}
            </button>
          ) : null}
        </div>
      </form>
    </section>
  );
}
