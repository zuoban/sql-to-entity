package cn.leftsite.sqltoentity.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(name = "cn.leftsite.sqltoentity.state.AppSettingsState", storages = @Storage("SqlToEntity.xml")
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {
    public String url;
    public String username;

    public static AppSettingsState getInstance() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Project project = openProjects.length > 0 ? openProjects[0]:ProjectManager.getInstance().getDefaultProject();
        return project.getService(AppSettingsState.class);
    }

    @Override
    public @NotNull AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
