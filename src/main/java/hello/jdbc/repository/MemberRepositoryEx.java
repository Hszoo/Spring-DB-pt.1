package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

public interface MemberRepositoryEx  {
    Member save(Member member) throws SQLException; // 인터페이스의 메서드 -> 체크 예외 throws -> 구현 클래스에서도 체크 예외 throws 가능
    Member findById(String memberId);
    void update(String memberId, int money);
    void delete(String memberId);
}
