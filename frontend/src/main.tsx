import { init } from '@tma.js/sdk-react';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import './index.css';

/**
 * Wire up Telegram Mini Apps SDK globals.
 * Outside Telegram (local dev in Chrome), this often throws — that is OK.
 */
try {
  init();
} catch (e) {
  console.warn('[FindTeam] Telegram SDK init skipped (normal in desktop browser).', e);
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
