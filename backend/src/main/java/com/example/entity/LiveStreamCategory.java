package com.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import com.example.util.entity.AbstractSoftDeletableEntity;

/**
 * ライブ配信に対するカテゴリ付けエンティティ。
 */
@Entity
@Table(name = "live_stream_categories",
       uniqueConstraints = @UniqueConstraint(name = "uk_live_stream_category",
                          columnNames = {"category_id", "live_stream_id"}),
       indexes = {
         @Index(name = "idx_live_stream_category_category_id", columnList = "category_id"),
         @Index(name = "idx_live_stream_category_live_stream_id", columnList = "live_stream_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE live_stream_categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Filter(name = "activeFilter", condition = "deleted_at IS NULL")
public class LiveStreamCategory extends AbstractSoftDeletableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  // ★ 追加：LiveStream への関連（mappedBy と対応）
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "live_stream_id", nullable = false)
  private LiveStream liveStream;

  // 参照用にIDも置きたい場合（任意。不要なら削除）
  // @Column(name = "live_stream_id", insertable = false, updatable = false)

  public LiveStreamCategory(Category category, LiveStream liveStream) {
    this.category = category;
    this.liveStream = liveStream;
  }
}
