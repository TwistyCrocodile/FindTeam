import type { ReactNode } from 'react';
import { TelegramUserStrip } from '../components/TelegramUserStrip';
import './AppLayout.css';

type Props = {
  children: ReactNode;
};

export function AppLayout({ children }: Props) {
  return (
    <div className="app-shell">
      <header className="app-header">
        <h1 className="app-title">FindTeam</h1>
        <p className="app-subtitle">Find teammates for your next project</p>
        <TelegramUserStrip />
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}
