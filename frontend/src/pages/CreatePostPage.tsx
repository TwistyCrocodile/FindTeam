import { CreatePostForm } from '../components/CreatePostForm';
import './CreatePostPage.css';

type Props = {
  telegramId: number;
  onCreated: () => void;
};

export function CreatePostPage({ telegramId, onCreated }: Props) {
  return (
    <section className="create-post-page">
      <CreatePostForm telegramId={telegramId} onCreated={onCreated} />
    </section>
  );
}

