import { retrieveLaunchParams } from '@tma.js/sdk-react';
import { useCallback, useEffect, useState } from 'react';
import { type Language, translations } from '../app/translations';

const LANGUAGE_STORAGE_KEY = 'findteam.language';
const LANGUAGE_CHANGED_EVENT = 'findteam-language-changed';

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

function resolveInitialLanguage(): Language {
  return readStoredLanguage() ?? readTelegramLanguage() ?? 'en';
}

function notifyLanguageChanged() {
  window.dispatchEvent(new Event(LANGUAGE_CHANGED_EVENT));
}

export function useLanguage() {
  const [language, setLanguageState] = useState<Language>(() => {
    if (typeof window === 'undefined') {
      return 'en';
    }
    return resolveInitialLanguage();
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
