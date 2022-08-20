package cn.leftsite.sqltoentity.service;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlToEntityService {
    private static final Pattern PATTERN = Pattern.compile("^\\s*`(.*)`.*COMMENT '(.*)'");
    private final Map<String, Map<String, String>> tableFieldCommentMap = new HashMap<>();
    private String url;
    private String user;
    private String password;

    public SqlToEntityService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public List<String> handle(String sql) throws SQLException {
        List<String> lines = new ArrayList<>();
        sql = sql.replace("\n", " ");
        sql = StringUtils.removeEnd(sql, ";");
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet resultSet = preparedStatement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 0; i < metaData.getColumnCount(); i++) {

                String columnName = metaData.getColumnName(i + 1).toLowerCase();
                String columnTypeName = metaData.getColumnTypeName(i + 1);
                String columnType;
                switch (columnTypeName) {
                    case "INTEGER":
                        columnType = "Integer";
                        break;
                    case "BIGINT":
                        columnType = "Long";
                        break;
                    case "DATE":
                    case "TIMESTAMP":
                        columnType = "Date";
                        break;
                    case "DECIMAL":
                        columnType = "BigDecimal";
                        break;
                    default:
                        columnType = "String";
                        break;
                }
                String tableName = metaData.getTableName(i + 1);
                Map<String, String> fieldComments = getFieldComments(tableName);
                String comment = fieldComments.get(columnName.toUpperCase());
                String filedDeclare = "";
                if (comment != null && comment.length() > 0) {
                    filedDeclare += "\n/**\n" + " * " + comment + "\n" + " */\n";
                }
                filedDeclare += "private " + columnType + " " + toCamelCase(columnName) + ";";
                lines.add(filedDeclare);
            }
            return lines;
        }
    }

    private Map<String, String> getFieldComments(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return Collections.emptyMap();
        }

        Map<String, String> fieldCommentsMap = tableFieldCommentMap.getOrDefault(tableName, new HashMap<>());
        if (!fieldCommentsMap.isEmpty()) {
            return fieldCommentsMap;
        }

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("show create table " + tableName); ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                String desc = resultSet.getString(2);
                String[] lines = desc.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    Matcher matcher = PATTERN.matcher(lines[i]);
                    if (matcher.find()) {
                        fieldCommentsMap.put(matcher.group(1).toUpperCase(), matcher.group(2));
                    }
                }
                tableFieldCommentMap.put(tableName, fieldCommentsMap);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return fieldCommentsMap;
    }

    private Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    private String toCamelCase(CharSequence name) {
        if (null == name) {
            return null;
        } else {
            String name2 = name.toString();
            if (name2.contains("_")) {
                int length = name2.length();
                StringBuilder sb = new StringBuilder(length);
                boolean upperCase = false;

                for (int i = 0; i < length; ++i) {
                    char c = name2.charAt(i);
                    if (c == '_') {
                        upperCase = true;
                    } else if (upperCase) {
                        sb.append(Character.toUpperCase(c));
                        upperCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                }

                return sb.toString();
            } else {
                return name2;
            }
        }
    }


}
