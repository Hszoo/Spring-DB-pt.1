package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/*
* 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
* */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection(); // 하나의 트랜잭션을 같은 세션에서 동작시키기 위해

        try {
            con.setAutoCommit(false); // 트랜잭션 시작

            // 비즈니스 로직 수행
            bizLogic(con, fromId, toId, money);

            // 정상 수행되었다면 commit
            con.commit();

        } catch (Exception e){
            // 예외 발생한 경우 Commit X /  rollback O
            con.rollback();
            throw new IllegalStateException(e);
        } finally {
            // 커넥션 close()
            release(con);
        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {

        // 여기서 부터
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        // 보내는 사람의 잔액 감소
        memberRepository.update(con, fromMember.getMemberId(), fromMember.getMoney() - money);

        // 임의로 에러 케이스 발생시킴
        validation(toMember); // AutoCommit mode : 예외 발생 이후 쿼리는 실행되지 않음 (보내는 사람의 잔액만 감소)

        // 받는 사람의 잔액 증가
        memberRepository.update(con, toMember.getMemberId(), toMember.getMoney() + money);

        // 여기까지가 하나의 비즈니스 로직 = 1 트랜잭션


        // 커밋 or 롤백

    }

    private static void release(Connection con) {
        if (con != null) {
            try {
                // AutoCommit(default) 상태로 변경
                // con.close() -> 종료된 커넥션이 풀에 반환되어 이후 재사용될 것을 고려하여
                con.setAutoCommit(true);
                con.close(); // 커넥션 종료 -> autoCommit 상태로 pool에 돌아감

            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private void validation(Member member) {
        if(member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }

    }


}
