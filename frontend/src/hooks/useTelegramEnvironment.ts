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

type TelegramWebAppWindow = Window & {
  Telegram?: {
    WebApp?: {
      initData?: string;
    };
  };
};

export function useTelegramEnvironment() {
  const [inTelegram, setInTelegram] = useState(false);
  const [user, setUser] = useState<TelegramUserSummary | null>(null);
  const [initData, setInitData] = useState<string | null>(null);

  useEffect(() => {
    if (!isTMA()) {
      setInTelegram(false);
      setUser(null);
      setInitData(null);
      return;
    }

    try {
      const lp = retrieveLaunchParams();
      const u = lp.tgWebAppData?.user;
      const rawInitData = (window as TelegramWebAppWindow).Telegram?.WebApp?.initData ?? null;
      setInitData(rawInitData || null);
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
      setInitData(null);
    }
  }, []);

  return { inTelegram, user, initData };
}
