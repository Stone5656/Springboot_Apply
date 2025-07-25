#!/bin/bash

# 引数：Entityのファイルパス
ENTITY_PATH="$1"
PACKAGE_ROOT="com.example.dto"

if [ ! -f "$ENTITY_PATH" ]; then
  echo "❌ Entity file not found: $ENTITY_PATH"
  exit 1
fi

# クラス名抽出（例: Video.java → Video）
ENTITY_FILENAME=$(basename -- "$ENTITY_PATH")
ENTITY_NAME="${ENTITY_FILENAME%.*}"

# スネークケース + s に変換（例: LiveStream → live_streams）
to_snake_case() {
  echo "$1" | sed -E 's/([a-z])([A-Z])/\1_\2/g' | tr '[:upper:]' '[:lower:]'
}
DTO_SUBDIR="$(to_snake_case "$ENTITY_NAME")s"

# 出力先ディレクトリとファイル名
DTO_DIR="src/main/java/$(echo $PACKAGE_ROOT | tr '.' '/')/${DTO_SUBDIR}"
DTO_FILE="${ENTITY_NAME}ResponseDTO.java"
DTO_PATH="$(realpath -m "$DTO_DIR/$DTO_FILE")"  # ← ここを絶対パス化

echo "実際のDTOパス: $DTO_PATH"
[ -f "$DTO_PATH" ] && echo "✔️ 存在してます" || echo "❌ 存在しません"

# DTOがすでに存在する場合はスキップ
if [ -f "$DTO_PATH" ]; then
  echo "⚠️  Skipped (already exists): $DTO_PATH"
  exit 0
fi

# 出力ディレクトリ作成
mkdir -p "$DTO_DIR"

# ファイル生成
cat <<EOF > "$DTO_PATH"
/**
 * 自動生成された ${ENTITY_NAME} のレスポンスDTO
 */
package ${PACKAGE_ROOT}.${DTO_SUBDIR};

import com.example.entity.${ENTITY_NAME};
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ${ENTITY_NAME}ResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ${ENTITY_NAME}ResponseDTO fromEntity(${ENTITY_NAME} entity) {
        return ${ENTITY_NAME}ResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
EOF

echo "✅ Generated: $DTO_PATH"
