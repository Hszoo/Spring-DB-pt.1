package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * 기본 동작, 트랜잭션이 없어서 문제 발생
 */
class MemberServiceV1Test {
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";


    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    @BeforeEach // 각 @Test 실행 전 호출됨
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        memberRepository = new MemberRepositoryV1(dataSource); // 의존성 주입
        memberService = new MemberServiceV1(memberRepository);
    }

    @AfterEach // 각 @Test 실행 후 호출됨
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A); // 각 테스트 실행 후 PK unique 에러 발생해서 삭제 처리
        memberRepository.delete(MEMBER_B);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws IOException, SQLException {
        // given : 사용자 생성
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when : 사용자 간 입금 시
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then : 두 사용자의 잔액 확인
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws IOException, SQLException {
        // given : 사용자 생성
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when : 사용자 간 입금 시 예외 발생한 경우
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then : 두 사용자의 잔액 확인
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000); // 보내는 사람의 잔액만 감소됨
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }

}