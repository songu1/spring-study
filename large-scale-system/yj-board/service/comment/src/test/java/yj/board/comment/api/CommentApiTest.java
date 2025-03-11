package yj.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import yj.board.comment.service.request.CommentCreateRequest;
import yj.board.comment.service.response.CommentPageResponse;
import yj.board.comment.service.response.CommentResponse;

import java.util.List;

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

    // 댓글 목록 조회 - 페이지 번호
    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment: response.getComments()) {
            // 2 depth 댓글이면 tab 문제
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.println("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /* 1번 페이지 수행 결과
        comment.getCommentId() = 155305721076776960

        comment.getCommentId() = 155305721286492161
        comment.getCommentId() = 155305721076776961

        comment.getCommentId() = 155305721286492165
        comment.getCommentId() = 155305721080971264

        comment.getCommentId() = 155305721286492160
        comment.getCommentId() = 155305721080971265

        comment.getCommentId() = 155305721286492162
        comment.getCommentId() = 155305721080971266

        comment.getCommentId() = 155305721286492164*/

    }

    // 댓글 목록 조회 - 무한스크롤
    @Test
    void readAllInfiniteScroll() {
        // 1 페이지
        List<CommentResponse> responses1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("first page");
        for (CommentResponse comment: responses1) {
            // 2 depth 댓글이면 tab 문제
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.println("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }


        // 2이상 페이지

        Long lastParentCommentId = responses1.getLast().getParentCommentId();
        Long lastCommentId = responses1.getLast().getCommentId();

        List<CommentResponse> responses2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastParentCommentId,lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("second page");
        for (CommentResponse comment: responses2) {
            // 2 depth 댓글이면 tab 문제
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.println("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

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
