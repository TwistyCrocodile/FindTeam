import { FormEvent, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { getFriendlyErrorMessage } from '../app/errors';
import { getUserStatusLabel } from '../app/translations';
import { useLanguage } from '../hooks/useLanguage';
import type { CreateUserProfileRequest, UserStatus } from '../types/user';
import './UserProfileForm.css';

const USER_STATUS_OPTIONS: UserStatus[] = ['LOOKING_FOR_TEAM', 'LOOKING_FOR_PROJECT', 'OPEN_TO_OFFERS', 'BUSY'];
const BIO_MAX_LENGTH = 1000;
const STACK_MAX_LENGTH = 500;

type ProfileValues = {
  nickname: string;
  bio: string;
  stack: string;
  githubUrl: string;
  status: UserStatus;
};

type Props = {
  initialValues: ProfileValues;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (values: Omit<CreateUserProfileRequest, 'telegramId'>) => Promise<void>;
};

function normalizeGithubUrl(value: string) {
  return value.trim();
}

function validateGithubUrl(value: string, messages: { httpUrl: string; validUrl: string }): string | null {
  const v = value.trim();
  if (!v) return null;
  try {
    const url = new URL(v);
    if (url.protocol !== 'http:' && url.protocol !== 'https:') return messages.httpUrl;
    return null;
  } catch {
    return messages.validUrl;
  }
}

export function UserProfileForm({ initialValues, submitLabel, onSubmit, loading, serverError }: Props) {
  const { t } = useLanguage();
  const [nickname, setNickname] = useState(initialValues.nickname);
  const [bio, setBio] = useState(initialValues.bio);
  const [stack, setStack] = useState(initialValues.stack);
  const [githubUrl, setGithubUrl] = useState(initialValues.githubUrl);
  const [status, setStatus] = useState<UserStatus>(initialValues.status);

  const [clientError, setClientError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const stackTextareaRef = useRef<HTMLTextAreaElement | null>(null);

  const nicknamePattern = useMemo(() => /^[A-Za-z0-9_.]{3,32}$/, []);

  useLayoutEffect(() => {
    resizeStackTextarea();
  }, [stack]);

  function resizeStackTextarea() {
    const textarea = stackTextareaRef.current;
    if (!textarea) return;

    textarea.style.height = 'auto';
    const maxHeight = Number.parseFloat(window.getComputedStyle(textarea).maxHeight);
    const nextHeight = Number.isFinite(maxHeight)
      ? Math.min(textarea.scrollHeight, maxHeight)
      : textarea.scrollHeight;
    textarea.style.height = `${nextHeight}px`;
  }

  function validate(): string | null {
    const n = nickname.trim();
    if (!n) return t.profile.nicknameRequired;
    if (!nicknamePattern.test(n)) return t.profile.nicknameInvalid;
    if (!stack.trim()) return t.createPost.stackRequired;
    if (bio.length > BIO_MAX_LENGTH) return t.profile.bioTooLong;
    if (stack.length > STACK_MAX_LENGTH) return t.profile.stackTooLong;
    const githubErr = validateGithubUrl(githubUrl, {
      httpUrl: t.profile.githubHttpUrl,
      validUrl: t.profile.githubValidUrl,
    });
    if (githubErr) return githubErr;
    return null;
  }

  async function onFormSubmit(e: FormEvent) {
    e.preventDefault();
    setClientError(null);

    const v = validate();
    if (v) {
      setClientError(v);
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit({
        nickname: nickname.trim(),
        bio: bio.trim() || undefined,
        stack: stack.trim(),
        githubUrl: normalizeGithubUrl(githubUrl) || undefined,
        status,
      } as Omit<CreateUserProfileRequest, 'telegramId'>);
    } catch (err) {
      const msg = getFriendlyErrorMessage(err, t.common.requestFailed, t);
      setClientError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="profile-form" onSubmit={onFormSubmit}>
      <label className="field">
        <span>{t.profile.nickname}</span>
        <input value={nickname} onChange={(e) => setNickname(e.target.value)} placeholder="e.g. dev_team_1" maxLength={32} />
      </label>

      <label className="field">
        <span>{t.profile.bioOptional}</span>
        <textarea value={bio} onChange={(e) => setBio(e.target.value)} rows={3} maxLength={BIO_MAX_LENGTH} />
        <span className="field__helper">{bio.length} / {BIO_MAX_LENGTH}</span>
      </label>

      <label className="field">
        <span>{t.profile.stack}</span>
        <textarea
          ref={stackTextareaRef}
          className="profile-form__stack-textarea"
          value={stack}
          onChange={(e) => setStack(e.target.value)}
          placeholder={'React\nSpring Boot\nDocker'}
          rows={3}
          maxLength={STACK_MAX_LENGTH}
        />
        <span className="field__helper">{stack.length} / {STACK_MAX_LENGTH}</span>
      </label>

      <label className="field">
        <span>{t.status.label}</span>
        <select value={status} onChange={(e) => setStatus(e.target.value as UserStatus)}>
          {USER_STATUS_OPTIONS.map((option) => (
            <option key={option} value={option}>
              {getUserStatusLabel(t, option)}
            </option>
          ))}
        </select>
      </label>

      <label className="field">
        <span>{t.profile.githubOptional}</span>
        <input value={githubUrl} onChange={(e) => setGithubUrl(e.target.value)} placeholder="https://github.com/..." maxLength={255} />
      </label>

      {clientError ? <p className="form-msg form-msg--error">{clientError}</p> : null}
      {serverError && !clientError ? <p className="form-msg form-msg--error">{serverError}</p> : null}

      <button type="submit" className="btn btn--primary" disabled={submitting || loading}>
        {submitting || loading ? t.profile.saving : submitLabel}
      </button>
    </form>
  );
}
