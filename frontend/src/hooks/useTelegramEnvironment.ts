import { isTMA, retrieveLaunchParams } from '@tma.js/sdk-react';
import { useEffect, useState } from 'react';

/**
 * Basic info about the Telegram user opening the Mini App (when available).
 * Uses the official SDK; safe to run in normal browser (falls back to "not in Telegram").
 */
export type TelegramUserSummary = {
  id: number;
  firstName: string;
  lastName?: string;
  username?: string;
};

export function useTelegramEnvironment() {
  const [inTelegram, setInTelegram] = useState(false);
  const [user, setUser] = useState<TelegramUserSummary | null>(null);

  useEffect(() => {
    if (!isTMA()) {
      setInTelegram(false);
      setUser(null);
      return;
    }

    try {
      const lp = retrieveLaunchParams();
      const u = lp.tgWebAppData?.user;
      if (u) {
        setInTelegram(true);
        setUser({
          id: u.id,
          firstName: u.first_name,
          lastName: u.last_name,
          username: u.username,
        });
      } else {
        setInTelegram(true);
        setUser(null);
      }
    } catch {
      setInTelegram(false);
      setUser(null);
    }
  }, []);

  return { inTelegram, user };
}
