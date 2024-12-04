package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;

/*
* 트랜잭션 - @Transcational AOP
* */

@Slf4j
//@Transactional : 클래스 내 public 메서드가 모두 AOP 의 대상이 됨
public class MemberServiceV3_3 { // 순수 서비스 로직만 남김

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional // 스프링 AOP 적용 : 성공시 커밋, 실패 시 롤백 자동
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //비즈니스 로직
        bizLogic(fromId, toId, money);
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {

        // 여기서 부터
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // 보내는 사람의 잔액 감소
        memberRepository.update(fromMember.getMemberId(), fromMember.getMoney() - money);

        // 임의로 에러 케이스 발생시킴
        validation(toMember); // AutoCommit mode : 예외 발생 이후 쿼리는 실행되지 않음 (보내는 사람의 잔액만 감소)

        // 받는 사람의 잔액 증가
        memberRepository.update(toMember.getMemberId(), toMember.getMoney() + money);

        // 여기까지가 하나의 비즈니스 로직 = 1 트랜잭션


        // 커밋 or 롤백

    }

    private void validation(Member member) {
        if(member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }

    }


}
