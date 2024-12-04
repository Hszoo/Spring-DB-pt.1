package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/*
* 트랜잭션 - 트랜잭션 템플릿
* */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_2 {

//    private final DataSource dataSource;
//    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
//        // 트랜잭션 시작
//        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        // 여기서 트랜잭션 시작  (반환 값 없을 때 사용하는 메서드) -> 실행 성공 : 자동 커밋, 실패(언체크 예외) 시 자동 롤백
        txTemplate.executeWithoutResult((status) -> {
            try {
                //비즈니스 로직
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e); // 언체크 예와
            }
        });

//        try {
//            // 비즈니스 로직 수행
//            bizLogic(fromId, toId, money);
//
//            // 정상 수행되었다면 commit
//            transactionManager.commit(status);
//
//        } catch (Exception e){
//            // 예외 발생한 경우 Commit X /  rollback O
//            transactionManager.rollback(status);
//            throw new IllegalStateException(e);
//        }  // 트랜잭션 매니저 : 트랜잭션 종료 시 알아서 close() -> 직접 close 하지 않아도됨
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
