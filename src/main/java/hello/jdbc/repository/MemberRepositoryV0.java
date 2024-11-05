package hello.jdbc.repository;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.NoSuchElementException;

/*
* JDBC - DriverManager 사용 (row level 동작 방식 확인)
* */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
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
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }

    }

    public Member findById(String memberId) throws SQLException {

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
            throw e;
        } finally {
            close(con, pstmt, rs); // 리소스 해제는 역순
        }
    }

    public void update(String memberId, int money) throws SQLException {
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
            throw e;
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }

    }

    public void delete(String memberId) throws SQLException {
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
            throw e;
        } finally { // close 호출이 보장되어야 하므로 finally에서 자원회수
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        // 각각이 널인 경우가 각각의 리소스 회수 하는 것에 영향을 미치지 않도록
        // 둘다 예외 처리를 했음

        if(rs != null) {
            try {
                rs.close(); //사용한 외부 리소스 닫아주기 (역순으로)
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(stmt != null) {
            try {
                stmt.close(); //사용한 외부 리소스 닫아주기 (역순으로)
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
