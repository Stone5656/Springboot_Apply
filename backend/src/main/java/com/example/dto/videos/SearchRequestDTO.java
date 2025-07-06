package com.example.dto.videos;

import com.example.enums.VideoSort;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequestDTO {

    private String query;          // タイトルの部分一致（任意）
    private VideoSort sortBy = VideoSort.PUBLISHED_AT;  // VIEWS_COUNT または PUBLISHED_AT（デフォルト：新着順）

}
