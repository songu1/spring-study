package yj.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import yj.board.article.service.response.ArticlePageResponse;
import yj.board.article.service.response.ArticleResponse;

import java.util.List;

public class ArticleApiTest {
    // 실행한 article application을 api로 호출하며 테스트
    // restclient는 spring boot 3이상에서 나온 http 클래스 호출
    // base url 지정
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi","my content", 1L, 1L
        ));
        System.out.println("response = "+response);
    }

    // restclient로 api 호출하는 메서드
    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()     // 호출
                .body(ArticleResponse.class);       // 응답 가져옴
    }

    @Test
    void readTest() {
        // createTest에서 생성한 articleId로 readTest
        ArticleResponse response = read(152846719819104256L);
        System.out.println("response = "+response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}",articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(152846719819104256L);
        ArticleResponse response = read(152846719819104256L);
        System.out.println("response = "+response);
    }

    void update(Long articleId) {
        restClient.put()
                .uri("/v1/articles/{articleId}",articleId)
                .body(new ArticleUpdateRequest("hi 2","m content 2"))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}",152846719819104256L)
                .retrieve();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&pageSize=30&page=50000")       // 1번게시판에서 30개의 게시글을 1번/50000번 페이지로 조회
                .retrieve()
                .body(ArticlePageResponse.class);       // articlepageresponse로 응답을 받음

        System.out.println("response.getArticleCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("articleId = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        // 첫번째 페이지
        List<ArticleResponse> articles1 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
                .retrieve()
                // 리스트로 반환이므로 ParameterizedTypeReference 사용
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        System.out.println("firstPage");
        for (ArticleResponse articleResponse : articles1) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }

        // 기준점이 있는 페이지
        Long lastArticleId = articles1.getLast().getArticleId();
        List<ArticleResponse> articles2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                // 리스트로 반환이므로 ParameterizedTypeReference 사용
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        System.out.println("secondPage");
        for (ArticleResponse articleResponse : articles2) {
            System.out.println("articleResponse.getArticleId() = " + articleResponse.getArticleId());
        }
    }


    // ArticleCreateRequest 정의
    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    // ArticleUpdateRequest 정의
    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
