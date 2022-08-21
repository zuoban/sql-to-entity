package cn.leftsite.sqltoentity.service;

import cn.leftsite.sqltoentity.util.JDBCUtil;
import cn.leftsite.sqltoentity.util.StrUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlToEntityService {
    private static final Pattern PATTERN = Pattern.compile("^\\s*`(.*)`.*COMMENT '(.*)'");
    private final Map<String, Map<String, String>> tableFieldCommentMap = new HashMap<>();
    private final String url;
    private final String user;
    private final String password;

    public SqlToEntityService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public List<String> handle(String sql) throws SQLException {
        List<String> lines = new ArrayList<>();
        sql = sql.replace("\n", " ");
        sql = StringUtils.removeEnd(sql, ";");
        try (Connection connection = JDBCUtil.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
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
                filedDeclare += "private " + columnType + " " + StrUtil.toCamelCase(columnName) + ";";
                lines.add(filedDeclare);
            }
            return lines;
        }
    }

    @SneakyThrows
    private Map<String, String> getFieldComments(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return Collections.emptyMap();
        }

        Map<String, String> fieldCommentsMap = tableFieldCommentMap.getOrDefault(tableName, new HashMap<>());
        if (!fieldCommentsMap.isEmpty()) {
            return fieldCommentsMap;
        }

        try (Connection connection = JDBCUtil.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement("show create table " + tableName);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String desc = resultSet.getString(2);
                String[] lines = desc.split("\n");
                for (String line : lines) {
                    Matcher matcher = PATTERN.matcher(line);
                    if (matcher.find()) {
                        fieldCommentsMap.put(matcher.group(1).toUpperCase(), matcher.group(2));
                    }
                }
                tableFieldCommentMap.put(tableName, fieldCommentsMap);
            }
        }
        return fieldCommentsMap;
    }
}
