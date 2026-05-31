import type { Translation } from './translations';

export function getFriendlyErrorMessage(error: unknown, fallback: string, t: Translation): string {
  if (error instanceof TypeError) {
    return t.common.connectionProblem;
  }

  if (error instanceof Error) {
    const message = error.message.trim();
    if (!message) {
      return fallback;
    }

    const normalized = message.toLowerCase();
    if (normalized.includes('failed to fetch') || normalized.includes('networkerror')) {
      return t.common.connectionProblem;
    }

    return message;
  }

  return fallback;
}
