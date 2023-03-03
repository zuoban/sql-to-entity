package cn.leftsite.sqltoentity.action;

import cn.leftsite.sqltoentity.service.SqlToEntityService;
import cn.leftsite.sqltoentity.ui.ShowEntityDialog;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class SqlToEntityAction extends AnAction {

    @SneakyThrows
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        SqlToEntityService sqlToEntityService = new SqlToEntityService(e.getProject());

        try {
            List<String> lines = sqlToEntityService.handle(selectedText);
            String content = StringUtils.join(lines, "\n");
            new ShowEntityDialog(content).show();
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
