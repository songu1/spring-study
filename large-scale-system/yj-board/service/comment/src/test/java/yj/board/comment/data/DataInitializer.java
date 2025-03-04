package yj.board.comment.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import yj.board.comment.entity.Comment;
import yj.board.common.snowflake.Snowflake;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class DataInitializer {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 6000;


    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for(int i = 0; i < EXECUTE_COUNT; i++) {
            executorService.submit(() -> {
                insert();
                latch.countDown();
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert() {
        transactionTemplate.executeWithoutResult(status -> {
            Comment prev = null;        // 이전 댓글을 null로 초기화
            for(int i = 0; i < BULK_INSERT_SIZE; i++) {
                Comment comment = Comment.create(
                        snowflake.nextId(),
                        "content",
                        i % 2 == 0 ? null : prev.getCommentId(),    // 짝수면 상위댓글x, 홀수면 이전댓글이 상위 댓글
                        1L,
                        1L
                );
                prev = comment;
                entityManager.persist(comment);
            }
        });
    }
}