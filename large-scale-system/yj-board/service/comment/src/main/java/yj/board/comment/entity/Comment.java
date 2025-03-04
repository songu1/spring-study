package yj.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name="comment")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId;     // shard key
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    // 팩토리 메서드
    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    // 1 depth인지를 확인하는 메서드
    public boolean isRoot() {
        return parentCommentId.longValue() == commentId;        // parentCommentId==commentId가 아닌 이유 : Long은 객체이므로 객체 비교가 아닌 값 비교를 위해서
    }

    // 삭제시 deleted = true로 설정
    public void delete() {
        deleted = true;
    }
}
