import { CreatePostForm } from '../components/CreatePostForm';
import './CreatePostPage.css';

type Props = {
  telegramId: number;
  initData?: string | null;
  onCreated: () => void;
};

export function CreatePostPage({ telegramId, initData, onCreated }: Props) {
  return (
    <section className="create-post-page">
      <CreatePostForm telegramId={telegramId} initData={initData} onCreated={onCreated} />
    </section>
  );
}

