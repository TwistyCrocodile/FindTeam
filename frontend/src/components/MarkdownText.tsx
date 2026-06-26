import ReactMarkdown, { type Components } from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './MarkdownText.css';

type Props = {
  children: string;
  className?: string;
};

const markdownComponents: Components = {
  a({ ...props }) {
    return <a {...props} target="_blank" rel="noopener noreferrer" />;
  },
};

export function MarkdownText({ children, className }: Props) {
  return (
    <div className={['markdown-text', className].filter(Boolean).join(' ')}>
      <ReactMarkdown components={markdownComponents} remarkPlugins={[remarkGfm]} skipHtml>
        {children}
      </ReactMarkdown>
    </div>
  );
}
