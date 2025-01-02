package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
 * 예외 누수 문제 해결
 * 체크 예외 -> 런타임 예외(언체크 예외)로 변경
 * MemberRepository 인터페이스 사용 & throws SQLException 제거
 * */
@Slf4j
public class MemberRepositoryV4_1 implements MemberRepository {

    private final DataSource dataSource; // 의존 관계 주입을 받아 Connection 획득에 사용

    public MemberRepositoryV4_1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override // 인터페이스 상속 -> 구현하는 경우 annot 사용해서 컴파일 타임에 문제 해결할 수 있도록
    public Member save(Member member)  {
        String sql  = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null; // 쿼리 날리는
        // PreparedStatement 파라미터 바인딩 가능 / Statement 는 불가

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            // return : 변경된 행의 수
            int count = pstmt.executeUpdate(); // 파라미터가 바인딩 된 쿼리가 데이터베이스에 전달됨
            // executeUpdate : 데이터 변경시 사용 / select 은 executeQuert()사용하면 됨

            return member;
        } catch (Exception e) {
            log.error("db error", e);
            throw new MyDBException(e);
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

            rs = pstmt.executeQuery(); // select 절은 데이터 변경이 없으므로 executeQuery로 실행 / return : resultSet , 셀렉 결과 담음
            if (rs.next()) { // next() 로 한번 이동한 다음 부터 진짜 데이터값 조회 가능 / 있으면 true 반환
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { // rs 에 데이터가 없는 경우
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw new MyDBException(e);
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
            throw new MyDBException(e);
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
            throw new MyDBException(e);
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 동기화 매니저의 Connection close 방식
        DataSourceUtils.releaseConnection(con, dataSource);
        // 커넥션은 이후 로직 & 트랜잭션 종료시 까지 살아있어야 됨
        // : con.close() X - 커넥션 유지 안됨
        // .releaseConnection() O - 트랜잭션을 위해 동기화된 커넥션은 유지 (서비스 계층에서 커넥션을 생성한 셈이라, 다른 계층에서는 close하지 않음)

//        JdbcUtils.closeConnection(con);

    }

    private Connection getConnection() {
        // 주의! 트랜잭션 동기화 : DataSourceUtils 사용
        Connection con = DataSourceUtils.getConnection(dataSource); // 트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 반환 / 없으면 생성 후 반환
        log.info("get Connection={}, Class={}, ", con, con.getClass());
        return con;
    }
}
