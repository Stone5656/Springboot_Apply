#!/bin/bash

ENTITY_DIR="src/main/java/com/example/entity"
SCRIPT_DIR="$(dirname "$0")"
GENERATOR="$SCRIPT_DIR/generate_dto.sh"

# 各Entityファイルに対してDTO生成
find "$ENTITY_DIR" -type f -name "*.java" | while read -r entity_file; do
  sh "$GENERATOR" "$entity_file"
done
