package cn.leftsite.sqltoentity.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.leftsite.sqltoentity.exception.ExecuteException;
import cn.leftsite.sqltoentity.util.JDBCUtil;
import com.intellij.database.remote.jdbc.RemoteResultSetMetaData;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlToEntityService {
    private static final Pattern PATTERN = Pattern.compile("^\\s*`(.*)`.*COMMENT '(.*)'");
    private final Project project;

    public SqlToEntityService(Project project) {
        this.project = project;
    }

    @SneakyThrows
    public List<String> handle(String sql) {
        sql = sql.replace("\n", " ");
        sql = StringUtils.removeEnd(sql, ";");
        return JDBCUtil.execute(project, sql,resultSet -> {
            List<String> result = new ArrayList<>();
            try {
                RemoteResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i + 1).toLowerCase();
                    String columnTypeName = metaData.getColumnTypeName(i + 1);
                    String columnType = switch (columnTypeName) {
                        case "INTEGER" -> "Integer";
                        case "BIGINT" -> "Long";
                        case "DATE", "TIMESTAMP" -> "Date";
                        case "DECIMAL" -> "BigDecimal";
                        default -> "String";
                    };
                    String tableName = metaData.getTableName(i + 1);
                    Map<String, String> fieldComments = getFieldComments(tableName);
                    String comment = fieldComments.get(columnName.toUpperCase());
                    String filedDeclare = "";
                    if (comment != null && comment.length() > 0) {
                        filedDeclare += "\n/**\n" + " * " + comment + "\n" + " */\n";
                    }
                    filedDeclare += "private " + columnType + " " + CharSequenceUtil.toCamelCase(columnName) + ";";
                    result.add(filedDeclare);
                }
            } catch (RemoteException | SQLException e) {
                throw new ExecuteException(e);
            }
            return result;
        });
    }

    @SneakyThrows
    private Map<String, String> getFieldComments(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return Collections.emptyMap();
        }

        String sql = "show create table " + tableName;
        return JDBCUtil.execute(project, sql, (resultSet -> {
            Map<String, String> fieldCommentsMap = new HashMap<>();
            try {
                if (resultSet.next()) {
                    String desc = resultSet.getString(2);
                    String[] lines = desc.split("\n");
                    for (String line : lines) {
                        Matcher matcher = PATTERN.matcher(line);
                        if (matcher.find()) {
                            fieldCommentsMap.put(matcher.group(1).toUpperCase(), matcher.group(2));
                        }
                    }
                }
            } catch (RemoteException | SQLException e) {
                throw new ExecuteException(e);
            }
            return fieldCommentsMap;
        }));
    }
}
