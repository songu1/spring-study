package yj.board.comment.service.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentPageResponse {
    private List<CommentResponse> comments;
    private Long commentCount;

    // 팩토리 메서드
    public static CommentPageResponse of(List<CommentResponse> comments, Long commentCount) {
        CommentPageResponse response = new CommentPageResponse();
        response.comments = comments;
        response.commentCount = commentCount;
        return response;
    }
}
