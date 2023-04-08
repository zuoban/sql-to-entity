package cn.leftsite.sqltoentity.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
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
import com.intellij.openapi.project.Project;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.function.Function;

public class JDBCUtil {
    private JDBCUtil() {
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static RemoteConnection getConnection(Project project) {
        // 通过数据库视图获取数据库连接
        DatabaseView databaseView = DatabaseView.getDatabaseView(project);
        DvTreeStructureService structureService = (DvTreeStructureService) ReflectUtil.getFieldValue(databaseView.getPanel(), "myStructureService");
        Map<LocalDataSource, Dbms> dsDbms = (Map<LocalDataSource, Dbms>) ReflectUtil.getFieldValue(structureService, "dsDbms");

        if (CollUtil.isEmpty(dsDbms)) {
            throw new RuntimeException("请先配置数据库连接");
        }

        LocalDataSource localDatasource = CollUtil.getFirst(dsDbms.keySet());
        // 这里做演示就只使用最后一个数据库连接
        //通过数据库连接管理创建连接
        GuardedRef<DatabaseConnection> connectionGuardedRef = DatabaseConnectionManager.getInstance().build(project, localDatasource).create();
        // 获取数据库连接
        if (connectionGuardedRef == null) {
            throw new RuntimeException("请先配置数据库连接");
        }
        return connectionGuardedRef.get().getRemoteConnection();
    }

    @SneakyThrows
    public static <T> T execute(Project project, String sql, Function<RemoteResultSet, T> function) {
        @Cleanup RemoteConnection connection = getConnection(project);
        @Cleanup RemotePreparedStatement remotePreparedStatement = connection.prepareStatement(sql);
        @Cleanup RemoteResultSet remoteResultSet = remotePreparedStatement.executeQuery();
        return function.apply(remoteResultSet);
    }
}
