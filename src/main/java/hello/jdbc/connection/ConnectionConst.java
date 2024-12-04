package hello.jdbc.connection;

public abstract class ConnectionConst { // 연결 정보를 남은 클래스 -> 객체 생성할 수 없도록 abstract
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}

