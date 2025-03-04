package yj.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import yj.board.comment.service.request.CommentCreateRequest;
import yj.board.comment.service.response.CommentResponse;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my comment2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my comment3", response1.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("commentId=%s".formatted(response2.getCommentId()));
        System.out.println("commentId=%s".formatted(response3.getCommentId()));

//        commentId=155295823691718656
//        commentId=155295843828572160
//        commentId=155295844092813312
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commendId}",155295823691718656L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
//        commentId=155295823691718656  - 1
//        commentId=155295843828572160  - 2
//        commentId=155295844092813312  - 3
        restClient.delete()
                .uri("/v1/comments/{commentId}",155295844092813312L)    // 실제 삭제는 안되고 deleted=true로 되어있어야함
                .retrieve();
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
