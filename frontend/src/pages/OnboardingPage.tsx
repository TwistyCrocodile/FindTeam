import { useMemo, useState } from 'react';
import { registerUser } from '../api/users';
import { UserProfileForm } from '../components/UserProfileForm';
import type { UserProfileResponse } from '../types/user';
import './OnboardingProfilePage.css';

type Props = {
  telegramId: number;
  nicknameSuggestion?: string | null;
  onCreated: (profile: UserProfileResponse) => void;
};

function sanitizeNicknameSuggestion(value: string | null | undefined) {
  const v = (value ?? '').trim().replace(/^@/, '');
  if (!v) return '';
  return /^[A-Za-z0-9_.]{3,32}$/.test(v) ? v : '';
}

export function OnboardingPage({ telegramId, nicknameSuggestion, onCreated }: Props) {
  const [serverError, setServerError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const initialValues = useMemo(
    () => ({
      nickname: sanitizeNicknameSuggestion(nicknameSuggestion),
      bio: '',
      stack: '',
      githubUrl: '',
    }),
    [nicknameSuggestion],
  );

  return (
    <section className="onboarding">
      <h2 className="onboarding__heading">Finish your profile</h2>
      <p className="onboarding__hint">
        Pick a nickname users can see in the app. Telegram username will not be shown publicly.
      </p>

      <UserProfileForm
        initialValues={initialValues}
        submitLabel="Create profile"
        loading={loading}
        serverError={serverError}
        onSubmit={async (values) => {
          setServerError(null);
          setLoading(true);
          try {
            const created = await registerUser({ telegramId, ...values });
            onCreated(created);
          } catch (e) {
            setServerError(e instanceof Error ? e.message : 'Request failed');
          } finally {
            setLoading(false);
          }
        }}
      />
    </section>
  );
}

