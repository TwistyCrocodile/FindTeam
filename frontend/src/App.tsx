import { AppLayout } from './app/AppLayout';
import { useTheme } from './hooks/useTheme';
import { HomePage } from './pages/HomePage';

export function App() {
  useTheme();

  return (
    <AppLayout>
      <HomePage />
    </AppLayout>
  );
}
