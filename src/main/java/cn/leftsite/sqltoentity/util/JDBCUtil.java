package cn.leftsite.sqltoentity.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.leftsite.sqltoentity.exception.ExecuteException;
import com.intellij.database.Dbms;
import com.intellij.database.dataSource.DatabaseConnection;
import com.intellij.database.dataSource.DatabaseConnectionManager;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.remote.jdbc.RemoteConnection;
import com.intellij.database.remote.jdbc.RemotePreparedStatement;
import com.intellij.database.remote.jdbc.RemoteResultSet;
import com.intellij.database.util.GuardedRef;
import com.intellij.database.view.DatabaseView;
import com.intellij.database.view.structure.DvTreeStructureService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class JDBCUtil {
    private JDBCUtil() {
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static @Nullable GuardedRef<DatabaseConnection> getConnection(Project project) {
        Set<LocalDataSource> localDataSources = new HashSet<>();
        // 通过数据库视图获取数据库连接
        ApplicationManager.getApplication().invokeAndWait(() -> {
            DatabaseView databaseView = DatabaseView.getDatabaseView(project);
            DvTreeStructureService structureService = (DvTreeStructureService) ReflectUtil.getFieldValue(databaseView.getPanel(), "myStructureService");
            Map<LocalDataSource, Dbms> dsDbms = (Map<LocalDataSource, Dbms>) ReflectUtil.getFieldValue(structureService, "dsDbms");
            if (CollUtil.isEmpty(dsDbms)) {
                throw new ExecuteException("请先配置数据库连接");
            }
            localDataSources.addAll(dsDbms.keySet());
        });


        LocalDataSource localDatasource = CollUtil.getLast(localDataSources);
        return DatabaseConnectionManager.getInstance().build(project, localDatasource).create();
    }

    @SneakyThrows
    public static <T> T execute(RemoteConnection connection, String sql, Function<RemoteResultSet, T> function) {
        @Cleanup RemotePreparedStatement remotePreparedStatement = connection.prepareStatement(sql);
        @Cleanup RemoteResultSet remoteResultSet = remotePreparedStatement.executeQuery();
        return function.apply(remoteResultSet);
    }
}
