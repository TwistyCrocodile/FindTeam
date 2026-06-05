import { useMemo, useState } from 'react';
import { registerCurrentUser, registerUser } from '../api/users';
import { getFriendlyErrorMessage } from '../app/errors';
import { UserProfileForm } from '../components/UserProfileForm';
import { useLanguage } from '../hooks/useLanguage';
import type { UserProfileResponse } from '../types/user';
import './OnboardingProfilePage.css';

type Props = {
  telegramId: number;
  initData?: string | null;
  nicknameSuggestion?: string | null;
  onCreated: (profile: UserProfileResponse) => void;
};

function sanitizeNicknameSuggestion(value: string | null | undefined) {
  const v = (value ?? '').trim().replace(/^@/, '');
  if (!v) return '';
  return /^[A-Za-z0-9_.]{3,32}$/.test(v) ? v : '';
}

export function OnboardingPage({ telegramId, initData, nicknameSuggestion, onCreated }: Props) {
  const { t } = useLanguage();
  const [serverError, setServerError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const initialValues = useMemo(
    () => ({
      nickname: sanitizeNicknameSuggestion(nicknameSuggestion),
      bio: '',
      stack: '',
      githubUrl: '',
      status: 'OPEN_TO_OFFERS' as const,
    }),
    [nicknameSuggestion],
  );

  return (
    <section className="onboarding">
      <h2 className="onboarding__heading">{t.onboarding.finishProfile}</h2>
      <p className="onboarding__tagline">{t.onboarding.tagline}</p>
      <p className="onboarding__hint">
        {t.onboarding.profileHint}
      </p>

      <UserProfileForm
        initialValues={initialValues}
        submitLabel={t.onboarding.createProfile}
        loading={loading}
        serverError={serverError}
        onSubmit={async (values) => {
          setServerError(null);
          setLoading(true);
          try {
            const created = initData
              ? await registerCurrentUser(initData, values)
              : await registerUser({ telegramId, ...values });
            onCreated(created);
          } catch (e) {
            setServerError(getFriendlyErrorMessage(e, t.common.requestFailed, t));
          } finally {
            setLoading(false);
          }
        }}
      />
    </section>
  );
}
