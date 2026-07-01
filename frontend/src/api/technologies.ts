import { apiUrl, parseErrorMessage } from './client';

export async function getTechnologies(): Promise<string[]> {
  const res = await fetch(apiUrl('/api/technologies'));
  if (!res.ok) throw new Error(await parseErrorMessage(res));
  return (await res.json()) as string[];
}
