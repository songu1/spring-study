package yj.board.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yj.board.article.entity.Article;
import yj.board.article.repository.ArticleRepository;
import yj.board.article.service.request.ArticleCreateRequest;
import yj.board.article.service.request.ArticleUpdateRequest;
import yj.board.article.service.response.ArticlePageResponse;
import yj.board.article.service.response.ArticleResponse;
import yj.board.common.snowflake.Snowflake;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;      // repository 주입

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(
                // id 생성 후 게시글 정보 넣기
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );
        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        articleRepository.deleteById(articleId);
    }

    // 페이지 활성화 번호 계산
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()    // offset 계산 공식
                        .map(ArticleResponse::from)     // articleresponse로 변환
                        .toList(),
                articleRepository.count(        // count도 반환
                        boardId,
                        PageLimitCalculator.calculatePageLimit(page, pageSize, 10L)     // 페이지 활성화 번호 공식 계산
                )
        );
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                articleRepository.findAllInfiniteScroll(boardId, pageSize) :        // 첫페이지 : boardId, pageSize만
                articleRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);      // 기준점o : + lastArticleId
        return articles.stream().map(ArticleResponse::from).toList();       // 쿼리 결과 응답
    }
}
