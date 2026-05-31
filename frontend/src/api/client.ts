/**
 * Base URL for the FindTeam API.
 * - Empty / unset: use same origin (works with Vite proxy in dev: `/api` → Spring Boot).
 * - Set to e.g. https://your-api.example.com for production or remote backend.
 */
export function getApiBaseUrl(): string {
  const raw = import.meta.env.VITE_API_BASE_URL ?? '';
  return raw.replace(/\/$/, '');
}

export function apiUrl(path: string): string {
  const base = getApiBaseUrl();
  const p = path.startsWith('/') ? path : `/${path}`;
  return base ? `${base}${p}` : p;
}

export function telegramInitDataHeaders(initData?: string | null): HeadersInit {
  return initData ? { 'X-Telegram-Init-Data': initData } : {};
}

/** Backend single-field error: { message, timestamp } */
export interface ErrorResponseBody {
  message: string;
  timestamp?: string;
}

/** Validation error: { message, timestamp, errors: { field: msg } } */
export interface ValidationErrorResponseBody {
  message: string;
  timestamp?: string;
  errors: Record<string, string>;
}

export async function parseErrorMessage(res: Response): Promise<string> {
  try {
    const data: unknown = await res.json();
    if (data && typeof data === 'object') {
      const v = data as ValidationErrorResponseBody;
      if (v.errors && typeof v.errors === 'object') {
        const first = Object.values(v.errors)[0];
        if (first) return String(first);
      }
      const e = data as ErrorResponseBody;
      if (e.message) return e.message;
    }
  } catch {
    /* ignore */
  }
  return res.statusText || `HTTP ${res.status}`;
}
