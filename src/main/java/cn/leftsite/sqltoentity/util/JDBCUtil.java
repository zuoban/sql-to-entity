package cn.leftsite.sqltoentity.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
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
            Set<LocalDataSource> localDataSources = new LinkedHashSet<>();
            for (DataSourceManager<?> manager : DataSourceManager.getManagers(project)) {
                for (RawDataSource dataSource : manager.getDataSources()) {
                    localDataSources.add((LocalDataSource) dataSource);
                }
            }
            localDatasource = CollUtil.getLast(localDataSources);
        } else {
            localDatasource = console.getDataSource();
            if (console.getSearchPath() != null && CollUtil.isNotEmpty(console.getSearchPath().elements)) {
                schema = CollUtil.getFirst(console.getSearchPath().elements).name;
                if (StrUtil.isNotEmpty(schema)) {
                    // 设置新的 schema
                    String jdbcUrl = localDatasource.getUrl();
                    String newJdbcUrl = Objects.requireNonNull(jdbcUrl).replaceAll("(/)([^/?#&]+)?([?].*)?$", "$1" + schema + "$3");
                    localDatasource.setUrl(newJdbcUrl);
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
