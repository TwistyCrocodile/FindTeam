import { useCallback, useEffect, useState } from 'react';

export type Theme = 'dark' | 'light';

const THEME_STORAGE_KEY = 'findteam.theme';

type TelegramThemeWindow = Window & {
  Telegram?: {
    WebApp?: {
      colorScheme?: Theme;
    };
  };
};

function isTheme(value: string | null | undefined): value is Theme {
  return value === 'dark' || value === 'light';
}

function readStoredTheme(): Theme | null {
  try {
    const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
    return isTheme(stored) ? stored : null;
  } catch {
    return null;
  }
}

function readTelegramTheme(): Theme | null {
  const colorScheme = (window as TelegramThemeWindow).Telegram?.WebApp?.colorScheme;
  return isTheme(colorScheme) ? colorScheme : null;
}

function resolveInitialTheme(): Theme {
  return readStoredTheme() ?? readTelegramTheme() ?? 'dark';
}

function applyTheme(theme: Theme) {
  document.documentElement.dataset.theme = theme;
}

export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(() => {
    if (typeof window === 'undefined') {
      return 'dark';
    }
    return resolveInitialTheme();
  });

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  const setTheme = useCallback((nextTheme: Theme) => {
    setThemeState(nextTheme);
    applyTheme(nextTheme);
    try {
      window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
    } catch {
      // localStorage can be unavailable in restricted WebViews; theme still updates for this session.
    }
  }, []);

  return { theme, setTheme };
}
