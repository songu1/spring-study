package yj.board.comment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yj.board.comment.entity.Comment;
import yj.board.comment.repository.CommentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)     // MockitoExtension으로 단위 테스트 작성
class CommentServiceTest {
    @InjectMocks
    CommentService commentService;      //
    @Mock
    CommentRepository commentRepository;        // mocking 객체로 commentRepository를 주입

    @Test
    @DisplayName("삭제할 댓글이 자식 있으면, 삭제 표시만 한다")
    void deleteShouldMarkDeletedIfHasChildren() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        // comment mocking 객체 생성
        Comment comment = createComment(articleId, commentId);
        // commentId로 찾은 객체가 mocking 객체일 때
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        // 하위댓글이 있는지
        given(commentRepository.countBy(articleId,commentId,2L)).willReturn(2L);

        // when
        commentService.delete(commentId);

        // then
        verify(comment).delete();
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제되지 않은 부모면, 하위 댓글만 삭제한다.")
    void deleteShouldDeleteChildOnlyIfNotDeletedParent() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        // comment mocking 객체 생성 - 상위 댓글이 있는 comment
        Comment comment = createComment(articleId, commentId, parentCommentId);
        // 하위 댓글 => root 댓글이 아님
        given(comment.isRoot()).willReturn(false);

        // parent mocking comment
        Comment parentComment = mock(Comment.class);
        // 삭제되지 않은 parent이므로 deleted=false
        given(parentComment.getDeleted()).willReturn(false);

        // commentId로 찾은 객체가 comment mocking 객체일 때
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        // 댓글 삭제(하위댓글 없음)
        given(commentRepository.countBy(articleId,commentId,2L)).willReturn(1L);    // hasChildren에서 false를 반환해주는 상황

        // parentCommentId로 찾은 객체가 parentComment mocking 객체일 때
        given(commentRepository.findById(parentCommentId))      // CommentServcie의 delete 메서드의 if문 안의 내용
                .willReturn(Optional.of(parentComment));

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);          // 하위 댓글은 삭제
        verify(commentRepository, never()).delete(parentComment);        // 상위 댓글은 삭제된 내역이 없으므로 delete가 호출되지 않음 (never():호출되지 않음을 검증)
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제된 부모면, 재귀적으로 모두 삭제한다.")
    void deleteShouldDeleteAllRecursivelyIfDeletedParent() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        // comment mocking 객체 생성 - 상위 댓글이 있는 comment
        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);              // 하위 댓글 => root 댓글이 아님

        // parent mocking comment
        Comment parentComment = createComment(articleId,parentCommentId);       // parentComment 새로 만들기
        given(parentComment.isRoot()).willReturn(true);         // parentComment는 root
        given(parentComment.getDeleted()).willReturn(true);    // 삭제된 parent이므로 deleted=true

        // commentId로 찾은 객체가 comment mocking 객체일 때
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        // 댓글 삭제(하위댓글 없음)
        given(commentRepository.countBy(articleId,commentId,2L)).willReturn(1L);    // hasChildren에서 false를 반환해주는 상황

        // parentCommentId로 찾은 객체가 parentComment mocking 객체일 때
        given(commentRepository.findById(parentCommentId))      // CommentServcie의 delete 메서드의 if문 안의 내용
                .willReturn(Optional.of(parentComment));
        // parentCommentId도 하위 댓글이 없는 상태
        given(commentRepository.countBy(articleId,parentCommentId,2L)).willReturn(1L);    // hasChildren에서 false를 반환해주는 상황

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);          // 하위 댓글은 삭제
        verify(commentRepository).delete(parentComment);        // 상위 댓글도 재귀적으로 삭제
    }

    // mock 객체 생성
    private Comment createComment(Long articleId, Long commentId) {
        // Comment 클래스의 Mock 객체를 생성 (실제 Comment를 만들지 않고 가짜 객체를 생성하여 동작을 테스트
        Comment comment = mock(Comment.class);
        // 전달 받은 파라미터를 mocking 객체에 넣어줌 (given - BDDMockito) : mock 객체의 동작 정의
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    // mock 객체 생성 (parentCommentId를 받는 경우)
    private Comment createComment(Long articleId, Long commentId, Long parentCommentId) {
        Comment comment = createComment(articleId,commentId);
        // 전달 받은 파라미터를 mocking 객체에 넣어줌
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }

}