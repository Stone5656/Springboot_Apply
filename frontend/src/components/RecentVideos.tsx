import React, { useEffect, useState } from 'react';
import { fetchRecentVideos } from '../api/videoApi';
import { PublicVideo } from '../api/models';

export const RecentVideos: React.FC = () => {
  const [videos, setVideos] = useState<PublicVideo[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const run = async () => {
      try {
        setLoading(true);
        setError(null);

        const page = await fetchRecentVideos(0, 12);
        setVideos(page.content);
      } catch (e: unknown) {
        console.error(e);
        setError(e.message ?? 'エラーが発生しました');
      } finally {
        setLoading(false);
      }
    };

    run();
  }, []);

  if (loading) return <div>読み込み中...</div>;
  if (error) return <div style={{ color: 'red' }}>エラー: {error}</div>;

  return (
    <div>
      <h2>新着動画</h2>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
          gap: '16px',
        }}
      >
        {videos.map((v) => (
          <div
            key={v.id}
            style={{
              border: '1px solid #ddd',
              borderRadius: 8,
              padding: 8,
              overflow: 'hidden',
            }}
          >
            {v.thumbnailPath && (
              <img
                src={v.thumbnailPath}
                alt={v.title}
                style={{ width: '100%', aspectRatio: '16/9', objectFit: 'cover' }}
              />
            )}
            <h3 style={{ fontSize: 16, margin: '8px 0' }}>{v.title}</h3>
            <p style={{ fontSize: 12, color: '#555' }}>
              {v.uploader?.name ?? 'Unknown'} ・ {v.viewsCount} 回再生
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};
