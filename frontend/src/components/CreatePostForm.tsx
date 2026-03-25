import { FormEvent, useState } from 'react';
import { createPost } from '../api/posts';
import type { CreatePostRequest, PostGoal, PostType } from '../types/post';
import './CreatePostForm.css';

const POST_TYPES: PostType[] = ['SEEKING_TEAM', 'SEEKING_MEMBER'];
const POST_GOALS: PostGoal[] = ['HACKATHON', 'PET_PROJECT', 'STARTUP', 'JOB'];

type Props = {
  telegramId: number;
  /** After a successful POST, parent refreshes the feed. */
  onCreated: () => void;
};

export function CreatePostForm({ telegramId, onCreated }: Props) {
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
    if (!Number.isFinite(telegramId) || telegramId <= 0) return 'Telegram ID must be a positive number.';
    if (!title.trim()) return 'Title is required.';
    if (!description.trim()) return 'Description is required.';
    if (!stack.trim()) return 'Stack is required.';
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
      await createPost(payload);
      setServerMessage('Post created.');
      setTitle('');
      setDescription('');
      setStack('');
      setEventLink('');
      onCreated();
    } catch (err) {
      setServerMessage(err instanceof Error ? err.message : 'Request failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="create-post">
      <h2 className="create-post__heading">Create a post</h2>
      <p className="create-post__hint">Posting as Telegram ID: {telegramId}</p>
      <form className="create-post__form" onSubmit={onSubmit}>
        <label className="field">
          <span>Type</span>
          <select value={type} onChange={(e) => setType(e.target.value as PostType)}>
            {POST_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>Title</span>
          <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={100} />
        </label>

        <label className="field">
          <span>Description</span>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} maxLength={1000} />
        </label>

        <label className="field">
          <span>Stack</span>
          <input value={stack} onChange={(e) => setStack(e.target.value)} maxLength={255} />
        </label>

        <label className="field">
          <span>Goal</span>
          <select value={goal} onChange={(e) => setGoal(e.target.value as PostGoal)}>
            {POST_GOALS.map((g) => (
              <option key={g} value={g}>
                {g}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>Event link (optional)</span>
          <input
            value={eventLink}
            onChange={(e) => setEventLink(e.target.value)}
            placeholder="https://..."
            maxLength={255}
          />
        </label>

        {clientError ? <p className="form-msg form-msg--error">{clientError}</p> : null}
        {serverMessage ? (
          <p className={`form-msg ${serverMessage.startsWith('Post created') ? 'form-msg--ok' : 'form-msg--error'}`}>
            {serverMessage}
          </p>
        ) : null}

        <button type="submit" className="btn btn--primary" disabled={submitting}>
          {submitting ? 'Submitting…' : 'Publish post'}
        </button>
      </form>
    </section>
  );
}
