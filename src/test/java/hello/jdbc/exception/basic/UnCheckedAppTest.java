package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void unChecked() {
        Controller controller = new Controller();

        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(RuntimeSQLException.class); // 하위 예외까지 모두 커버
    }

    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            // e.printStackTrace(); 호출 가능
            log.info("ex", e);

        }
    }

    static class Controller {
        Service service = new Service();

        public void request() { // 모두 uncheck 예외를 발생시켜 throws 하지 않음
            service.logic();
        }
    }

    static class Service {

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() { // 모두 uncheck 예외를 발생시켜 throws 하지 않음
            repository.call();
            networkClient.call();

        }
    }

    static class NetworkClient {
        public void call() {
            throw new RunTimeConnectException("연결 실패"); //check exception
        }
    }

    static class Repository {
        public void call() {
            try {
                runSQL(); // 체크 예외 발생 (SQLException)
            } catch (SQLException e) {
                // 체크 예외 -> 언체크 예외 RuntimeSQLException으로 throw
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RunTimeConnectException extends RuntimeException { // uncheck exception
        public RunTimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException { // uncheck exception

        public RuntimeSQLException() { // 상위 예외 객체를 받음
            super();
        }
        public RuntimeSQLException(Throwable cause) { // 상위 예외 객체를 받음
            super(cause);
        }
    }
}
