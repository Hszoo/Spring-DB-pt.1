package hello.jdbc.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;

public class CheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();

        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class); // 하위 예외까지 모두 커버
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {

        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();

        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패"); //check exception
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("ex"); //check exception
        }
    }
}
