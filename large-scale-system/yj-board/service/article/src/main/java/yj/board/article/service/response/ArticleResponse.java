package yj.board.article.service.response;

import lombok.Getter;
import lombok.ToString;
import yj.board.article.entity.Article;

import java.time.LocalDateTime;

@Getter
@ToString
public class ArticleResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;       // shard key
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 팩토리 메서드 생성
    public static ArticleResponse from(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.articleId = article.getArticleId();
        response.title = article.getTitle();
        response.content = article.getContent();
        response.boardId = article.getBoardId();
        response.writerId = article.getWriterId();
        response.createdAt = article.getCreatedAt();
        return response;
    }
}
