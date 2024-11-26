package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {
    @Test /* 드라이버 매니저로 connection 획득하기 */
    void driverManager() throws SQLException {
        // DB 커넥션 연결 2회
        Connection con1 = DriverManager.getConnection(URL,USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL,USERNAME, PASSWORD);

        // 커넥션 정보 확인
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    @Test /* 스프링부트 제공 매니저로 Connection 획득하기 */
    void DataSourceeManager() throws SQLException {
        // DriverManagerdataSource는 항상 새로운 연결 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        userDataSource(dataSource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링 by. HikariCP
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("My Pool");

        userDataSource(dataSource);
        // 커넥션 풀에서 커넥션을 생성하는 작업은 별도의 스레드에서 실행됨 로그 확인 위해 sleep 걸었음
        Thread.sleep(100);
    }

    // datasource 인터페이스를 통해 Connection 획득
    private void userDataSource(DataSource dataSource) throws SQLException {
        Connection conn1 = dataSource.getConnection();
        Connection conn2 = dataSource.getConnection();
    }

}
