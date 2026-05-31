import { useLanguage } from '../hooks/useLanguage';
import { useTelegramEnvironment } from '../hooks/useTelegramEnvironment';
import './TelegramUserStrip.css';

/**
 * Shows when the app runs inside Telegram and init data includes the user.
 * In plain browser dev, shows a short hint instead.
 */
export function TelegramUserStrip() {
  const { t } = useLanguage();
  const { inTelegram, user, initData } = useTelegramEnvironment();

  if (!inTelegram) {
    return (
      <div className="tg-strip tg-strip--dev">
        <span className="tg-strip__label">{t.onboarding.devMode}</span>
        <span className="tg-strip__hint">{t.onboarding.openInTelegramHint}</span>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="tg-strip">
        <span className="tg-strip__label">Telegram</span>
        <span className="tg-strip__hint">
          {initData ? t.onboarding.telegramAuthDetected : t.onboarding.userDataMissing}
        </span>
      </div>
    );
  }

  const display = [user.firstName, user.lastName].filter(Boolean).join(' ');
  const labelFallback = t.onboarding.telegramUser;

  return (
    <div className="tg-strip tg-strip--user">
      <span className="tg-strip__label">{t.onboarding.signedInAs}</span>
      <span className="tg-strip__name">
        {display || labelFallback}
      </span>
      <span className="tg-strip__hint">
        {initData ? t.onboarding.telegramAuthDetected : t.onboarding.telegramAuthMissing}
      </span>
    </div>
  );
}
