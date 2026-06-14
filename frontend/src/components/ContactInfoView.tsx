import type { ContactInfoResponse } from '../types/user';
import { useLanguage } from '../hooks/useLanguage';

type Props = {
  contact: ContactInfoResponse;
  emptyMessage?: string;
};

export function hasContactInfo(contact: ContactInfoResponse | null) {
  return Boolean(contact?.contactTelegramUsername || contact?.contactEmail);
}

export function ContactInfoView({ contact, emptyMessage = 'User has not added contact info yet.' }: Props) {
  const { t } = useLanguage();

  if (!hasContactInfo(contact)) {
    const message = emptyMessage === 'User has not added contact info yet.' ? t.contact.userNoContactInfo : emptyMessage;
    return <p className="contact-info__empty">{message}</p>;
  }

  return (
    <dl className="contact-info">
      {contact.contactTelegramUsername ? (
        <div className="contact-info__row">
          <dt>Telegram</dt>
          <dd>@{contact.contactTelegramUsername}</dd>
        </div>
      ) : null}
      {contact.contactEmail ? (
        <div className="contact-info__row">
          <dt>Email</dt>
          <dd>
            <a href={`mailto:${contact.contactEmail}`}>{contact.contactEmail}</a>
          </dd>
        </div>
      ) : null}
    </dl>
  );
}
