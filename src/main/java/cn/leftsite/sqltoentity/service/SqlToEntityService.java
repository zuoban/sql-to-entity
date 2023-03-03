package cn.leftsite.sqltoentity.service;

import cn.hutool.core.util.StrUtil;
import cn.leftsite.sqltoentity.util.JDBCUtil;
import com.intellij.database.remote.jdbc.RemoteConnection;
import com.intellij.database.remote.jdbc.RemotePreparedStatement;
import com.intellij.database.remote.jdbc.RemoteResultSet;
import com.intellij.database.remote.jdbc.RemoteResultSetMetaData;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlToEntityService {
    private static final Pattern PATTERN = Pattern.compile("^\\s*`(.*)`.*COMMENT '(.*)'");
    private final Map<String, Map<String, String>> tableFieldCommentMap = new HashMap<>();
    private final Project project;

    public SqlToEntityService(@Nullable Project project) {
        this.project = project;
    }


    @SneakyThrows
    public List<String> handle(String sql) throws SQLException {
        List<String> lines = new ArrayList<>();
        sql = sql.replace("\n", " ");
        sql = StringUtils.removeEnd(sql, ";");
        RemoteConnection connection = JDBCUtil.getConnection(project);
        RemotePreparedStatement preparedStatement = connection.prepareStatement(sql);
        RemoteResultSet resultSet = preparedStatement.executeQuery();
        RemoteResultSetMetaData metaData = resultSet.getMetaData();
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

    @SneakyThrows
    private Map<String, String> getFieldComments(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return Collections.emptyMap();
        }

        Map<String, String> fieldCommentsMap = tableFieldCommentMap.getOrDefault(tableName, new HashMap<>());
        if (!fieldCommentsMap.isEmpty()) {
            return fieldCommentsMap;
        }

        RemoteConnection connection = JDBCUtil.getConnection(project);
        RemotePreparedStatement preparedStatement = connection.prepareStatement("show create table " + tableName);
        RemoteResultSet resultSet = preparedStatement.executeQuery();
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
        return fieldCommentsMap;
    }
}
