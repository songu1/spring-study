package yj.board.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.board.comment.entity.Comment;
import yj.board.comment.repository.CommentRepository;
import yj.board.comment.service.request.CommentCreateRequest;
import yj.board.comment.service.response.CommentPageResponse;
import yj.board.comment.service.response.CommentResponse;
import yj.board.common.snowflake.Snowflake;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;      // comment repository 주입 받기

    // 댓글 생성 메서드
    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        // parent 찾기
        Comment parent = findParent(request);
        // comment 저장
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return null;
        }
        return commentRepository.findById(parentCommentId)      // 상위 댓글 찾기
                .filter(not(Comment::getDeleted))               // 상위 댓글이 삭제가 되지 않은 상태인지 확인 (deleted 필드를 확인)
                .filter(Comment::isRoot)                        // 2-depth이므로 상위 댓글이 root 댓글인지 확인
                .orElseThrow();                                 // 아니라면 상위 댓글이 아니므로 예외를 던지기
    }

    // 댓글 조회 메서드
    public CommentResponse read(Long commentId) {
        // 데이터가 없으면 예외를 던져주도록
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    // 댓글 삭제
    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)           // 댓글 id로 댓글 찾기
                .filter(not(Comment::getDeleted))       // 아직 삭제되지 않은 댓글인지 확인
                .ifPresent(comment -> {                 // 댓글이 있으면 삭제
                    if (hasChildren(comment)) {         // 하위 댓글이 있다면 삭제 표시만 해줌
                        comment.delete();
                    } else {                            // 하위 댓글이 없다면 삭제
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        // commentId를 parentCommentId로 가지는 comment들의 수를 세기 (인수: commentId, 파라미터:parentCommentId가 됨)
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
        // 본인댓글(root인 경우), 자식이 1개 이상인지 확인하기 위해 2개만 조회
    }

    private void delete(Comment comment) {
        // 삭제
        commentRepository.delete(comment);
        // 하위 댓글이 삭제가 됐으면 deleted=true, 하위 댓글이 없는 상위 댓글을 재귀적으로 삭제
        if(!comment.isRoot()) {
            commentRepository.findById(comment.getParentCommentId())        // 상위 댓글
                    .filter(Comment::getDeleted)                            // 상위댓글이 삭제가 되어있는지 확인
                    .filter(not(this::hasChildren))                         // 상위댓글이 또 다른 자식을 가지고 있지 않은지 확인
                    .ifPresent(this::delete);                               // 재귀적으로 상위 댓글 삭제
        }
    }

    /* 댓글 목록 조회 - 페이지 번호 */
    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                // 댓글 조회
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)         // 응답을 CommentResponse로 변환
                        .toList(),                          // 리스트로 변환
                // 댓글 개수 조회
                commentRepository.count(articleId,PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))  // 한 화면에서 이동가능한 페이지 번호 수 : 10L로 지정
        );
    }

    /* 댓글 목록 조회 - 무한스크롤 */
    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        // 1페이지
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
                commentRepository.findAllInfiniteScroll(articleId, limit) :                                     // 1페이지
                commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);  // 2이상 페이지
        return comments.stream()
                .map(CommentResponse::from)     // List<Comment> 를 List<CommentResponse> 변환
                .toList();
    }
}
