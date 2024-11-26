package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // 보내는 사람의 잔액 감소
        memberRepository.update(fromMember.getMemberId(), fromMember.getMoney() - money);

        // 임의로 에러 케이스 발생시킴
        validation(toMember); // AutoCommit mode : 예외 발생 이후 쿼리는 실행되지 않음 (보내는 사람의 잔액만 감소)

        // 받는 사람의 잔액 증가
        memberRepository.update(toMember.getMemberId(), toMember.getMoney() + money);
    }

    private void validation(Member member) {
        if(member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }

    }


}
