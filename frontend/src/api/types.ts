// HTTP メソッドを限定
export type HttpMethod =
  | 'get'
  | 'post'
  | 'put'
  | 'delete'
  | 'patch'
  | 'head'
  | 'options';

export interface EndpointSpec {
  tag: string;                // OpenAPI の tags[0]（例: "Videos"）
  operationId?: string;       // "getRecentVideos" など
  url: string;                // 例: "http://localhost:9000/api/videos/recent"
  method: HttpMethod;         // 例: "get"
  description: string;        // summary / description （両方あれば description 優先）

  // request_body / response_body は schema をそのまま持たせる（anyでOK）
  requestBodySchema?: unknown;
  responseBodySchema?: unknown;
}
