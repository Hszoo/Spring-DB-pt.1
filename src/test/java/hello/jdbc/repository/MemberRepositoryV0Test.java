package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 memberRepositoryV0 = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // save
        Member memberV0 = new Member("memberV9", 10000);
        memberRepositoryV0.save(memberV0);

        // findById
        Member findMember = memberRepositoryV0.findById(memberV0.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(memberV0);

        // update : money: 10000->20000
        memberRepositoryV0.update(memberV0.getMemberId(), 20000);
        Member updatedMember = memberRepositoryV0.findById(memberV0.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        memberRepositoryV0.delete(memberV0.getMemberId());
        // 없는 멤버 조회시 예외 발생 (no such element .. 예외 우리가 작성한대로) -> Assertions 실행시 예외가 발생하도록
        // 따라서 삭제 검증 : 없는 회원 조회시 발생하는 예외를 확인하면 되는 것
        Assertions.assertThatThrownBy(() -> memberRepositoryV0.findById(memberV0.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}