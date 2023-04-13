package cn.leftsite.sqltoentity.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class MyNotifier {
    private MyNotifier() {
        throw new IllegalStateException("Utility class");
    }

    public static void notify(Project project, NotificationType notificationType, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("sql-to-entity.notification.group.id")
                .createNotification(content, notificationType)
                .notify(project);
    }
}