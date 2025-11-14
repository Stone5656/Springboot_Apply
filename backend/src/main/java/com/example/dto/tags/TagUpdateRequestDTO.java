package com.example.dto.tags;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * タグ更新用リクエストDTO（PATCH 的な柔軟更新）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagUpdateRequestDTO {

    @Size(max = 100)
    private String name;   // null なら変更なし

    // slug をクライアントから更新させない方針ならフィールドを持たない
    // もし slug も更新したいなら以下を追加
    @Size(max = 255)
    private String slug; // null なら変更なし
}
