import { isTMA, retrieveLaunchParams, retrieveRawInitData } from '@tma.js/sdk-react';
import { useEffect, useState } from 'react';

/**
 * Basic info about the Telegram user opening the Mini App (when available).
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

/**
 * Raw init data string for {@code X-Telegram-Init-Data} (URL-encoded query string).
 * Prefer @tma.js bridge (launch params); fall back to classic WebApp global when present.
 */
function readInitDataFromSources(): string | null {
  try {
    const fromSdk = retrieveRawInitData();
    if (fromSdk?.trim()) {
      return fromSdk.trim();
    }
  } catch {
    // SDK may throw outside Telegram — try WebApp global next.
  }

  const fromWebApp = (window as TelegramWebAppWindow).Telegram?.WebApp?.initData;
  if (fromWebApp?.trim()) {
    return fromWebApp.trim();
  }

  return null;
}

function logTelegramDiagnostics(snapshot: {
  inTelegram: boolean;
  hasInitData: boolean;
  hasUserId: boolean;
}) {
  if (!import.meta.env.DEV) {
    return;
  }
  console.info('[FindTeam] Telegram environment', snapshot);
}

export function useTelegramEnvironment() {
  const [inTelegram, setInTelegram] = useState(false);
  const [user, setUser] = useState<TelegramUserSummary | null>(null);
  const [initData, setInitData] = useState<string | null>(null);

  useEffect(() => {
    const rawInitData = readInitDataFromSources();
    const insideTelegram = isTMA() || Boolean(rawInitData);

    if (!insideTelegram) {
      setInTelegram(false);
      setUser(null);
      setInitData(null);
      logTelegramDiagnostics({ inTelegram: false, hasInitData: false, hasUserId: false });
      return;
    }

    setInTelegram(true);
    setInitData(rawInitData);

    let telegramUser: TelegramUserSummary | null = null;
    try {
      const lp = retrieveLaunchParams();
      const u = lp.tgWebAppData?.user;
      if (u) {
        telegramUser = {
          id: u.id,
          firstName: u.first_name,
          lastName: u.last_name,
          username: u.username,
        };
      }
    } catch {
      // Secure endpoints can still use raw init data even if launch params parsing fails.
    }

    setUser(telegramUser);
    logTelegramDiagnostics({
      inTelegram: true,
      hasInitData: Boolean(rawInitData),
      hasUserId: Boolean(telegramUser?.id),
    });
  }, []);

  return { inTelegram, user, initData };
}
