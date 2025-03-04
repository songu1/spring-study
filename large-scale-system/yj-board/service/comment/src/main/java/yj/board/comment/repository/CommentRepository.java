package yj.board.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yj.board.comment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // native query로 children 여부 확인 - 특정 게시글에서 해당 parentCommendId의 댓글 개수를 가져올 수 있음 => 자식 개수 구하기
    // commentId를 parentCommentId로 가지는 comment들의 수를 세기 (인수: commentId, 파라미터:parentCommentId가 됨)
    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment " +          // comment id만 뽑아와서 covering index로 동작 가능
                    "   where article_id = :articleId and parent_comment_id = :parentCommentId " +
                    "   limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long countBy(
            @Param("articleId") Long articleId,
            @Param("parentCommentId") Long parentCommentId,
            @Param("limit") Long limit
    );
}
