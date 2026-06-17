import { retrieveLaunchParams } from '@tma.js/sdk-react';
import { useCallback, useEffect, useState } from 'react';
import { type Language, translations } from '../app/translations';
import type { PreferredLanguage } from '../types/user';

const LANGUAGE_STORAGE_KEY = 'findteam.language';
const LANGUAGE_CHANGED_EVENT = 'findteam-language-changed';
let preferredLanguageInitialized = false;

type TelegramWebAppWindow = Window & {
  Telegram?: {
    WebApp?: {
      initDataUnsafe?: {
        start_param?: string;
      };
    };
  };
};

function isLanguage(value: string | null | undefined): value is Language {
  return value === 'en' || value === 'ru';
}

function normalizeLanguageCode(languageCode: string | null | undefined): Language | null {
  if (!languageCode) {
    return null;
  }
  return languageCode.toLowerCase().startsWith('ru') ? 'ru' : 'en';
}

function readStoredLanguage(): Language | null {
  try {
    const stored = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
    return isLanguage(stored) ? stored : null;
  } catch {
    return null;
  }
}

function readTelegramLanguage(): Language | null {
  try {
    const user = retrieveLaunchParams().tgWebAppData?.user;
    return normalizeLanguageCode(user?.language_code);
  } catch {
    return null;
  }
}

function preferredLanguageToLanguage(preferredLanguage: PreferredLanguage | null | undefined): Language | null {
  if (preferredLanguage === 'RU') {
    return 'ru';
  }
  if (preferredLanguage === 'EN') {
    return 'en';
  }
  return null;
}

function normalizeStartParameter(value: string | null | undefined): PreferredLanguage | null {
  const normalized = value?.trim().toLowerCase();
  if (normalized === 'lang_ru' || normalized === 'ru') {
    return 'RU';
  }
  if (normalized === 'lang_en' || normalized === 'en') {
    return 'EN';
  }
  return null;
}

export function readPreferredLanguageFromStartParameter(): PreferredLanguage | null {
  try {
    const fromLaunchParams = normalizeStartParameter(retrieveLaunchParams().tgWebAppStartParam);
    if (fromLaunchParams) {
      return fromLaunchParams;
    }
  } catch {
    // SDK can throw outside Telegram; fall back to classic WebApp and URL parameters.
  }

  const fromWebApp = normalizeStartParameter(
    (window as TelegramWebAppWindow).Telegram?.WebApp?.initDataUnsafe?.start_param,
  );
  if (fromWebApp) {
    return fromWebApp;
  }

  try {
    const params = new URLSearchParams(window.location.search);
    return (
      normalizeStartParameter(params.get('tgWebAppStartParam')) ??
      normalizeStartParameter(params.get('startapp')) ??
      normalizeStartParameter(params.get('lang'))
    );
  } catch {
    return null;
  }
}

function resolveInitialLanguage(options?: { includeStartParameter?: boolean }): Language {
  const startParameterLanguage = options?.includeStartParameter
    ? preferredLanguageToLanguage(readPreferredLanguageFromStartParameter())
    : null;
  return startParameterLanguage ?? readStoredLanguage() ?? readTelegramLanguage() ?? 'en';
}

function notifyLanguageChanged() {
  window.dispatchEvent(new Event(LANGUAGE_CHANGED_EVENT));
}

function normalizePreferredLanguage(preferredLanguage: PreferredLanguage | null | undefined): Language | null {
  return preferredLanguageToLanguage(preferredLanguage);
}

export function initializeLanguageFromPreferredLanguage(preferredLanguage: PreferredLanguage | null | undefined) {
  if (preferredLanguageInitialized) {
    return;
  }

  const nextLanguage = normalizePreferredLanguage(preferredLanguage);
  if (!nextLanguage) {
    return;
  }

  preferredLanguageInitialized = true;
  try {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, nextLanguage);
  } catch {
    // Storage can be unavailable in restricted WebViews; notify active hooks anyway.
  }
  notifyLanguageChanged();
}

export function useLanguage() {
  const [language, setLanguageState] = useState<Language>(() => {
    if (typeof window === 'undefined') {
      return 'en';
    }
    return resolveInitialLanguage({ includeStartParameter: true });
  });

  useEffect(() => {
    function onLanguageChanged() {
      setLanguageState(resolveInitialLanguage());
    }

    window.addEventListener(LANGUAGE_CHANGED_EVENT, onLanguageChanged);
    window.addEventListener('storage', onLanguageChanged);
    return () => {
      window.removeEventListener(LANGUAGE_CHANGED_EVENT, onLanguageChanged);
      window.removeEventListener('storage', onLanguageChanged);
    };
  }, []);

  const setLanguage = useCallback((nextLanguage: Language) => {
    try {
      window.localStorage.setItem(LANGUAGE_STORAGE_KEY, nextLanguage);
    } catch {
      // Storage can be unavailable in restricted WebViews; keep the in-memory state updated.
    }
    setLanguageState(nextLanguage);
    notifyLanguageChanged();
  }, []);

  return { language, setLanguage, t: translations[language] };
}
