import { FormEvent, useMemo, useState } from 'react';
import { getFriendlyErrorMessage } from '../app/errors';
import { useLanguage } from '../hooks/useLanguage';
import type { CreateUserProfileRequest } from '../types/user';
import './UserProfileForm.css';

type ProfileValues = {
  nickname: string;
  bio: string;
  stack: string;
  githubUrl: string;
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

  const [clientError, setClientError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const nicknamePattern = useMemo(() => /^[A-Za-z0-9_.]{3,32}$/, []);

  function validate(): string | null {
    const n = nickname.trim();
    if (!n) return t.profile.nicknameRequired;
    if (!nicknamePattern.test(n)) return t.profile.nicknameInvalid;
    if (!stack.trim()) return t.createPost.stackRequired;
    if (bio.length > 500) return t.profile.bioTooLong;
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
        <textarea value={bio} onChange={(e) => setBio(e.target.value)} rows={3} maxLength={500} />
      </label>

      <label className="field">
        <span>{t.profile.stack}</span>
        <input value={stack} onChange={(e) => setStack(e.target.value)} placeholder="React, Spring Boot..." maxLength={255} />
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

