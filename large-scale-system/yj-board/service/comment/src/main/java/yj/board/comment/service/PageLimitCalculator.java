package yj.board.comment.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// 페이지 번호 활성화에 필요한 카운트 계산 공식 추가 - 유틸성 클래스-private 생성자,final 클래스
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageLimitCalculator {

    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }

}
