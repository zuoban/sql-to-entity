package cn.leftsite.sqltoentity.factory;

import cn.leftsite.sqltoentity.state.AppSettingsState;
import cn.leftsite.sqltoentity.ui.SettingsUI;
import cn.leftsite.sqltoentity.util.PasswordStoreUtil;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class SettingsFactory implements SearchableConfigurable {
    private SettingsUI settingsUI = new SettingsUI();

    @Override
    public @NotNull @NonNls String getId() {
        return "sql-to-entity.id";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Sql To Entity";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return settingsUI.getSettingPanel();
    }

    @Override
    public boolean isModified() {

        AppSettingsState state = AppSettingsState.getInstance().getState();

        Credentials credentials = PasswordStoreUtil.retrieveCredentials(state.username);
        boolean passwordChanged = false;
        if (credentials != null) {
            passwordChanged = !Objects.equals(credentials.getPasswordAsString(), settingsUI.getPasswordText());
        }
        return !Objects.equals(state.url, settingsUI.getUrlText())
                || !Objects.equals(state.username, settingsUI.getUsernameText())
                || passwordChanged;
    }

    @Override
    public void apply() {
        AppSettingsState state = AppSettingsState.getInstance().getState();
        state.url = settingsUI.getUrlText();
        String usernameText = settingsUI.getUsernameText();
        String passwordText = settingsUI.getPasswordText();
        state.username = usernameText;
        PasswordStoreUtil.storeCredentials(usernameText, passwordText);
    }

    @Override
    public void reset() {
        AppSettingsState state = AppSettingsState.getInstance().getState();
        settingsUI.setUrlText(state.url);
        settingsUI.setUsernameText(state.username);

        Credentials credentials = PasswordStoreUtil.retrieveCredentials(state.username);
        if (credentials != null) {
            settingsUI.setPasswordText(credentials.getPasswordAsString());
        }
    }
}