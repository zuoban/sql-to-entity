package cn.leftsite.sqltoentity.util;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;

public class JDBCUtil {
    private JDBCUtil() {
    }


    @SneakyThrows
    public static Connection getConnection(String url, String user, String password) {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }
}
