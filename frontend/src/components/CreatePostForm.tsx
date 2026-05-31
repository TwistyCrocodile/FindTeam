import { FormEvent, useState } from 'react';
import { createPostAuthAware } from '../api/posts';
import { getFriendlyErrorMessage } from '../app/errors';
import { getPostGoalLabel, getPostTypeLabel } from '../app/translations';
import { useLanguage } from '../hooks/useLanguage';
import type { CreatePostRequest, PostGoal, PostType } from '../types/post';
import './CreatePostForm.css';

const POST_TYPES: PostType[] = ['SEEKING_TEAM', 'SEEKING_MEMBER'];
const POST_GOALS: PostGoal[] = ['HACKATHON', 'PET_PROJECT', 'STARTUP', 'JOB'];

type Props = {
  telegramId: number;
  initData?: string | null;
  /** After a successful POST, parent refreshes the feed. */
  onCreated: () => void;
};

export function CreatePostForm({ telegramId, initData, onCreated }: Props) {
  const { t } = useLanguage();
  const [type, setType] = useState<PostType>('SEEKING_TEAM');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [stack, setStack] = useState('');
  const [goal, setGoal] = useState<PostGoal>('HACKATHON');
  const [eventLink, setEventLink] = useState('');

  const [clientError, setClientError] = useState<string | null>(null);
  const [serverMessage, setServerMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

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

    const payload: CreatePostRequest = {
      telegramId,
      type,
      title: title.trim(),
      description: description.trim(),
      stack: stack.trim(),
      goal,
      eventLink: eventLink.trim() || undefined,
    };

    setSubmitting(true);
    try {
      await createPostAuthAware(payload, initData);
      setServerMessage(t.createPost.created);
      setTitle('');
      setDescription('');
      setStack('');
      setEventLink('');
      onCreated();
    } catch (err) {
      setServerMessage(getFriendlyErrorMessage(err, t.common.requestFailed, t));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="create-post">
      <h2 className="create-post__heading">{t.createPost.heading}</h2>
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
          <p className={`form-msg ${serverMessage === t.createPost.created ? 'form-msg--ok' : 'form-msg--error'}`}>
            {serverMessage}
          </p>
        ) : null}

        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? t.createPost.submitting : t.createPost.submit}
        </button>
      </form>
    </section>
  );
}
