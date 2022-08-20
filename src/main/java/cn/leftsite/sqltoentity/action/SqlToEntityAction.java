package cn.leftsite.sqltoentity.action;

import cn.leftsite.sqltoentity.dialog.SampleDialogWrapper;
import cn.leftsite.sqltoentity.service.SqlToEntityService;
import cn.leftsite.sqltoentity.state.AppSettingsState;
import cn.leftsite.sqltoentity.util.PasswordStoreUtil;
import com.intellij.credentialStore.Credentials;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class SqlToEntityAction extends AnAction {

    private SqlToEntityService sqlToEntityService;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        AppSettingsState state = AppSettingsState.getInstance();
        if (StringUtils.isAnyBlank(state.url, state.username)) {
            showNotification(e.getProject(), "请检查数据库连接配置", NotificationType.ERROR);
            return;
        }

        Credentials credentials = PasswordStoreUtil.retrieveCredentials(state.username);
        if (credentials == null) {
            showNotification(e.getProject(), "请检查数据库连接配置", NotificationType.ERROR);
            return;
        }
        String password = credentials.getPasswordAsString();

        sqlToEntityService = new SqlToEntityService(state.url, state.username, password);

        try {
            List<String> lines = sqlToEntityService.handle(selectedText);
            String content = StringUtils.join(lines, "\n");
            new SampleDialogWrapper(content).show();
        } catch (SQLException ex) {
            showNotification(e.getProject(), ex.getMessage(), NotificationType.ERROR);
        }
    }

    private void showNotification(Project project, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("sql-to-entity.notification.group.id")
                .createNotification(content, type)
                .notify(project);
    }
}
