package cn.leftsite.sqltoentity.util;

import com.intellij.database.console.JdbcConsole;
import com.intellij.database.console.JdbcConsoleProvider;
import com.intellij.database.dataSource.DatabaseConnection;
import com.intellij.database.dataSource.DatabaseConnectionManager;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.model.RawDataSource;
import com.intellij.database.psi.DataSourceManager;
import com.intellij.database.remote.jdbc.RemoteConnection;
import com.intellij.database.remote.jdbc.RemotePreparedStatement;
import com.intellij.database.remote.jdbc.RemoteResultSet;
import com.intellij.database.util.GuardedRef;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Function;

public class JDBCUtil {
    private JDBCUtil() {
    }

    @SneakyThrows
    public static @Nullable GuardedRef<DatabaseConnection> getConnection(@NotNull Project project, PsiFile psiFile) {
        JdbcConsole console = null;

        if (psiFile != null) {
            console = JdbcConsoleProvider.getConsole(project, psiFile.getViewProvider().getVirtualFile());
        }

        String schema = null;
        LocalDataSource localDatasource;
        if (console == null) {
            LinkedHashSet<LocalDataSource> localDataSources = new LinkedHashSet<>();
            for (DataSourceManager<?> manager : DataSourceManager.getManagers(project)) {
                for (RawDataSource dataSource : manager.getDataSources()) {
                    localDataSources.add((LocalDataSource) dataSource);
                }
            }
            // 选择最后一个数据源
            localDatasource = localDataSources.stream().skip(localDataSources.size() - 1).findFirst().orElse(null);
        } else {
            localDatasource = console.getDataSource();
            if (console.getSearchPath() != null && CollectionUtils.isNotEmpty(console.getSearchPath().elements)) {
                schema = console.getSearchPath().elements.get(0).name;
                if (StringUtils.isNotEmpty(schema)) {
                    // 设置新的 schema
                    String jdbcUrl = localDatasource.getUrl();
                    if (StringUtils.countMatches(jdbcUrl, '/') == 3) {
                        String newJdbcUrl = Objects.requireNonNull(jdbcUrl).replaceAll("(/)([^/?#&]+)?([?].*)?$", "$1" + schema + "$3");
                        localDatasource.setUrl(newJdbcUrl);
                    } else {
                        String newJdbcUrl = jdbcUrl + "/" + schema;
                        localDatasource.setUrl(newJdbcUrl);
                    }
                }

            }
        }
        GuardedRef<DatabaseConnection> databaseConnectionGuardedRef = DatabaseConnectionManager.getInstance().build(project, localDatasource).create();
        if (schema != null) {
            Objects.requireNonNull(databaseConnectionGuardedRef).get().getRemoteConnection().setSchema(schema);
        }
        return databaseConnectionGuardedRef;
    }

    @SneakyThrows
    public static <T> T execute(RemoteConnection connection, String sql, Function<RemoteResultSet, T> function) {
        @Cleanup RemotePreparedStatement remotePreparedStatement = connection.prepareStatement(sql);
        @Cleanup RemoteResultSet remoteResultSet = remotePreparedStatement.executeQuery();
        return function.apply(remoteResultSet);
    }
}
