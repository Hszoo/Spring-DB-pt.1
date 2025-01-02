package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource; // 의존 관계 주입을 받아 Connection 획득에 사용
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new
                SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override // 인터페이스 상속 -> 구현하는 경우 annot 사용해서 컴파일 타임에 문제 해결할 수 있도록
    public Member save(Member member)  {
        String sql  = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            // return : 변경된 행의 수
            int count = pstmt.executeUpdate(); // 파라미터가 바인딩 된 쿼리가 데이터베이스에 전달됨

            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw exTranslator.translate("save", sql, e);
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }

    }

    @Override
    public Member findById(String memberId) {

        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { // rs 에 데이터가 없는 경우
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs); // 리소스 해제는 역순
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;


        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate(); // 쿼리 실행 후 변경된 row 의 개수 return
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw exTranslator.translate("update", sql, e);
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }

    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw exTranslator.translate("delete", sql, e);
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 동기화 매니저의 Connection close 방식
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() {
        // 주의! 트랜잭션 동기화 : DataSourceUtils 사용
        Connection con = DataSourceUtils.getConnection(dataSource); // 트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 반환 / 없으면 생성 후 반환
        log.info("get Connection={}, Class={}, ", con, con.getClass());
        return con;
    }
}
