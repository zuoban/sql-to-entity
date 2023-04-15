package cn.leftsite.sqltoentity.action;

import cn.hutool.core.lang.Assert;
import cn.leftsite.sqltoentity.service.SqlToEntityService;
import cn.leftsite.sqltoentity.ui.ShowEntityDialog;
import cn.leftsite.sqltoentity.util.JDBCUtil;
import cn.leftsite.sqltoentity.util.MyNotifier;
import com.intellij.database.dataSource.DatabaseConnection;
import com.intellij.database.remote.jdbc.RemoteConnection;
import com.intellij.database.util.GuardedRef;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class SqlToEntityAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();
            Assert.notBlank(selectedText, "Please select the SQL to be executed.");

            Project project = e.getProject();
            PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Sql to entity") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        indicator.setText("Obtaining a data connection");
                        GuardedRef<DatabaseConnection> guardedRef = JDBCUtil.getConnection(Objects.requireNonNull(e.getProject()), psiFile);
                        RemoteConnection remoteConnection = Objects.requireNonNull(guardedRef).get().getRemoteConnection();
                        indicator.setText("Processing SQL");
                        perform(remoteConnection, selectedText);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        MyNotifier.notify(e.getProject(), NotificationType.ERROR, ex.getMessage());
                    }
                }
            });
        } catch (Exception ex) {
            MyNotifier.notify(e.getProject(), NotificationType.ERROR, ex.getMessage());
        }
    }

    private static void perform(@NotNull RemoteConnection remoteConnection, String selectedText) {
        SqlToEntityService sqlToEntityService = new SqlToEntityService(remoteConnection);
        List<String> lines = sqlToEntityService.handle(selectedText);
        String content = StringUtils.join(lines, "\n");
        ApplicationManager.getApplication().invokeLater(() -> new ShowEntityDialog(content).show());
    }
}
