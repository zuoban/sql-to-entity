package cn.leftsite.sqltoentity.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ReflectUtil;
import com.intellij.database.dataSource.AbstractDataSource;
import com.intellij.database.dataSource.DatabaseConnection;
import com.intellij.database.dataSource.DatabaseConnectionManager;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.model.basic.BasicNode;
import com.intellij.database.remote.jdbc.RemoteConnection;
import com.intellij.database.util.GuardedRef;
import com.intellij.database.view.DataSourceNode;
import com.intellij.database.view.DatabaseView;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;

import javax.swing.tree.TreeModel;
import java.util.TreeSet;

public class JDBCUtil {
    private JDBCUtil() {
    }

    private static TimedCache<Object, RemoteConnection> connectionCache = CacheUtil.newTimedCache(300_000);

    @SneakyThrows
    public static RemoteConnection getConnection(Project project) {
        if (connectionCache.containsKey(project)) {
            return connectionCache.get(project);
        }
        DatabaseView databaseView = DatabaseView.getDatabaseView(project);
        TreeModel model = databaseView.getTree().getModel();
        TreeSet<BasicNode> children = (TreeSet<BasicNode>) ReflectUtil.getFieldValue(model.getRoot(), "children");
        if (children == null || children.isEmpty()) {
            throw new RuntimeException("please config database connection on database view");
        }

        // 这里做演示就只使用最后一个数据库连接
        AbstractDataSource realDataSource = ((DataSourceNode) children.last()).realDataSource;
        LocalDataSource localDataSource = (LocalDataSource) realDataSource;
        //通过数据库连接管理创建连接
        GuardedRef<DatabaseConnection> connectionGuardedRef = DatabaseConnectionManager.getInstance().build(project, localDataSource).create();
        // 获取数据库连接
        DatabaseConnection databaseConnection = connectionGuardedRef.get();
        RemoteConnection remoteConnection = databaseConnection.getRemoteConnection();
        connectionCache.put(project, remoteConnection);
        return remoteConnection;
    }
}
