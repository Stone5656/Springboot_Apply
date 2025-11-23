import rawSpec from './api-docs.json';
import { EndpointSpec, HttpMethod } from './types';

// サーバーURL（なければ空文字）
const SERVER_URL: string =
  (rawSpec as any).servers?.[0]?.url ?? '';

const paths = (rawSpec as any).paths ?? {};

// OpenAPI の paths から EndpointSpec[] を作る
const endpointList: EndpointSpec[] = [];

for (const [path, methods] of Object.entries<any>(paths)) {
  for (const [method, op] of Object.entries<any>(methods)) {
    const lowerMethod = method.toLowerCase() as HttpMethod;

    // application/json 優先、なければ "*/*"
    const reqSchema =
      op.requestBody?.content?.['application/json']?.schema ??
      op.requestBody?.content?.['*/*']?.schema;

    const resSchema =
      op.responses?.['200']?.content?.['application/json']?.schema ??
      op.responses?.['200']?.content?.['*/*']?.schema;

    const description: string =
      (op.description as string | undefined) ??
      (op.summary as string | undefined) ??
      '';

    const tag = (op.tags && op.tags[0]) || 'default';

    endpointList.push({
      tag,
      operationId: op.operationId,
      url: SERVER_URL + path,   // フルURLとして持っておく
      method: lowerMethod,
      description,
      requestBodySchema: reqSchema,
      responseBodySchema: resSchema,
    });
  }
}

export const endpoints: EndpointSpec[] = endpointList;

// operationId で検索
export function findEndpointByOperationId(operationId: string): EndpointSpec | undefined {
  return endpoints.find((e) => e.operationId === operationId);
}

// URL + method で検索したい場合
export function findEndpoint(url: string, method: HttpMethod): EndpointSpec | undefined {
  return endpoints.find((e) => e.url.endsWith(url) && e.method === method);
}
