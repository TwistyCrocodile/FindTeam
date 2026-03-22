import { useTelegramEnvironment } from '../hooks/useTelegramEnvironment';
import './TelegramUserStrip.css';

/**
 * Shows when the app runs inside Telegram and init data includes the user.
 * In plain browser dev, shows a short hint instead.
 */
export function TelegramUserStrip() {
  const { inTelegram, user } = useTelegramEnvironment();

  if (!inTelegram) {
    return (
      <div className="tg-strip tg-strip--dev">
        <span className="tg-strip__label">Dev mode</span>
        <span className="tg-strip__hint">Open inside Telegram for real user context.</span>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="tg-strip">
        <span className="tg-strip__label">Telegram</span>
        <span className="tg-strip__hint">User data not in launch params.</span>
      </div>
    );
  }

  const display = [user.firstName, user.lastName].filter(Boolean).join(' ');
  const handle = user.username ? `@${user.username}` : `id ${user.id}`;

  return (
    <div className="tg-strip tg-strip--user">
      <span className="tg-strip__label">Signed in as</span>
      <span className="tg-strip__name">
        {display || handle}
        {display && user.username ? ` (${handle})` : null}
      </span>
    </div>
  );
}
