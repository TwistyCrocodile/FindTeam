import type { ContactInfoResponse, UserProfileResponse } from '../types/user';

type ContactProfile = Pick<UserProfileResponse, 'githubUrl'>;

function hasText(value?: string | null) {
  return Boolean(value?.trim());
}

export function hasAnyContact(profile?: ContactProfile | null, contactInfo?: ContactInfoResponse | null) {
  return Boolean(
    hasText(profile?.githubUrl)
    || hasText(contactInfo?.contactTelegramUsername)
    || hasText(contactInfo?.contactEmail),
  );
}
