import type { ContactInfoResponse } from '../types/user';

type Props = {
  contact: ContactInfoResponse;
  emptyMessage?: string;
};

export function hasContactInfo(contact: ContactInfoResponse | null) {
  return Boolean(contact?.contactTelegramUsername || contact?.contactGithubUrl || contact?.contactEmail);
}

export function ContactInfoView({ contact, emptyMessage = 'User has not added contact info yet.' }: Props) {
  if (!hasContactInfo(contact)) {
    return <p className="contact-info__empty">{emptyMessage}</p>;
  }

  return (
    <dl className="contact-info">
      {contact.contactTelegramUsername ? (
        <div className="contact-info__row">
          <dt>Telegram</dt>
          <dd>@{contact.contactTelegramUsername}</dd>
        </div>
      ) : null}
      {contact.contactGithubUrl ? (
        <div className="contact-info__row">
          <dt>GitHub</dt>
          <dd>
            <a href={contact.contactGithubUrl} target="_blank" rel="noreferrer">
              {contact.contactGithubUrl}
            </a>
          </dd>
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

