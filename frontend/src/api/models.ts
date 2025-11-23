// 最小限のページング型
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 現在ページ
  size: number;
}

// PublicVideoResponseDTO 相当（必要な項目だけ）
export interface PublicVideo {
  id: string;
  title: string;
  thumbnailPath?: string;
  viewsCount: number;
  uploader: {
    id: string;
    name: string;
    profileImagePath?: string;
  };
}
