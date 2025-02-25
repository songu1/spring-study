package yj.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import yj.board.article.service.response.ArticleResponse;

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
