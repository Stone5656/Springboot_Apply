import { findEndpointByOperationId } from './endpoints';
import { callEndpoint } from './client';
import { Page, PublicVideo } from './models';

// operationId は OpenAPI 中の "getRecentVideos"
const getRecentVideosSpec = findEndpointByOperationId('getRecentVideos');

if (!getRecentVideosSpec) {
  // ロード失敗時はアプリ起動時に気づけるようにしておく
  console.error('Endpoint spec not found for getRecentVideos');
}

export async function fetchRecentVideos(page = 0, size = 12) {
  if (!getRecentVideosSpec) throw new Error('getRecentVideos spec not loaded');

  return callEndpoint<Page<PublicVideo>>(getRecentVideosSpec, {
    query: { page, size },
  });
}
