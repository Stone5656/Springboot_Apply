import axios from 'axios';
import { EndpointSpec } from './types';

// axios インスタンス
export const apiClient = axios.create({
  // baseURL は EndpointSpec.url に既に含まれているので指定なしでもOK
  // もしくは rawSpec.servers[0].url を baseURL にして、EndpointSpec は path のみでもいい
});

// 呼び出しオプション
export interface CallOptions {
  // URL 中の {id} とか {videoId} を埋める用
  pathParams?: Record<string, string | number>;
  // ?page=0&size=20 みたいなクエリ
  query?: Record<string, string | number | boolean | undefined>;
  // JSON ボディ
  body?: unknown;
  // 任意のヘッダ（Bearer Token 等）
  headers?: Record<string, string>;
}

function buildUrlWithPathParams(spec: EndpointSpec, pathParams?: CallOptions['pathParams']): string {
  if (!pathParams) return spec.url;

  return spec.url.replace(/\{(\w+)\}/g, (_, key: string) => {
    const value = pathParams[key];
    if (value === undefined || value === null) {
      throw new Error(`Missing path param: ${key}`);
    }
    return encodeURIComponent(String(value));
  });
}

export async function callEndpoint<TResponse = unknown>(
  spec: EndpointSpec,
  options: CallOptions = {}
): Promise<TResponse> {
  const url = buildUrlWithPathParams(spec, options.pathParams);

  return apiClient
    .request<TResponse>({
      url,
      method: spec.method,
      params: options.query,
      data: options.body,
      headers: options.headers,
    })
    .then((res: { data: never; }) => res.data);
}
