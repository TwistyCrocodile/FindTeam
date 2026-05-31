import { useCallback, useEffect, useState } from 'react';
import {
  acceptApplicationAuthAware,
  getApplicationsForPostAuthAware,
  rejectApplicationAuthAware,
} from '../api/applications';
import type { ApplicationResponse } from '../types/application';
import './PostApplicationsPanel.css';

type Props = {
  postId: number;
  ownerTelegramId: number;
  initData?: string | null;
  onClose: () => void;
};

function formatWhen(iso: string) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

function formatStatus(status: ApplicationResponse['status']) {
  if (status === 'PENDING') return 'Pending review';
  if (status === 'ACCEPTED') return 'Accepted';
  return 'Rejected';
}

export function PostApplicationsPanel({ postId, ownerTelegramId, initData, onClose }: Props) {
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await getApplicationsForPostAuthAware(postId, ownerTelegramId, initData);
      setApplications(list);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load applications');
      setApplications([]);
    } finally {
      setLoading(false);
    }
  }, [postId, ownerTelegramId, initData]);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleAccept(id: number) {
    setActionError(null);
    setBusyId(id);
    try {
      const updated = await acceptApplicationAuthAware(id, ownerTelegramId, initData);
      setApplications((prev) => prev.map((a) => (a.id === id ? updated : a)));
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not accept');
    } finally {
      setBusyId(null);
    }
  }

  async function handleReject(id: number) {
    setActionError(null);
    setBusyId(id);
    try {
      const updated = await rejectApplicationAuthAware(id, ownerTelegramId, initData);
      setApplications((prev) => prev.map((a) => (a.id === id ? updated : a)));
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Could not reject');
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="post-applications">
      <div className="post-applications__head">
        <h4 className="post-applications__title">Applications</h4>
        <button type="button" className="post-applications__close" onClick={onClose}>
          Close
        </button>
      </div>

      {loading ? <p className="post-applications__state">Loading…</p> : null}
      {!loading && error ? <p className="post-applications__state post-applications__state--error">{error}</p> : null}
      {!loading && !error && applications.length === 0 ? (
        <p className="post-applications__state">No applications yet.</p>
      ) : null}

      <ul className="post-applications__list">
        {applications.map((a) => (
          <li key={a.id} className={`post-applications__item post-applications__item--${a.status.toLowerCase()}`}>
            <div className="post-applications__item-main">
              <strong>{a.applicantNickname}</strong>
              <span className={`post-applications__status post-applications__status--${a.status.toLowerCase()}`}>
                {formatStatus(a.status)}
              </span>
            </div>
            <p className="post-applications__time">{formatWhen(a.createdAt)}</p>
            {a.status === 'PENDING' ? (
              <div className="post-applications__actions">
                <button
                  type="button"
                  className="post-applications__btn post-applications__btn--accept"
                  disabled={busyId !== null}
                  onClick={() => void handleAccept(a.id)}
                >
                  {busyId === a.id ? '…' : 'Accept'}
                </button>
                <button
                  type="button"
                  className="post-applications__btn post-applications__btn--reject"
                  disabled={busyId !== null}
                  onClick={() => void handleReject(a.id)}
                >
                  {busyId === a.id ? '…' : 'Reject'}
                </button>
              </div>
            ) : null}
            {a.status === 'ACCEPTED' && a.contactAvailable ? (
              <div className="post-applications__contact">
                <p className="post-applications__contact-note">Contact will be available in a future version.</p>
                <button type="button" className="post-applications__btn post-applications__btn--contact" disabled>
                  Contact (soon)
                </button>
              </div>
            ) : null}
          </li>
        ))}
      </ul>

      {actionError ? <p className="post-applications__state post-applications__state--error">{actionError}</p> : null}
    </div>
  );
}
