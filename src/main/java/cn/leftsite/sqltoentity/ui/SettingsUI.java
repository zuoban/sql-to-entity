package cn.leftsite.sqltoentity.ui;

import javax.swing.*;


public class SettingsUI {
    private JTextField urlTextField;
    private JTextField usernameTextField;
    private JPanel settingPanel;
    private JPasswordField passwordField;

    public JPanel getSettingPanel() {
        return settingPanel;
    }

    public String getUrlText() {
        return urlTextField.getText();
    }

    public void setUrlText(String urlText) {
        urlTextField.setText(urlText);
    }

    public String getUsernameText() {
        return usernameTextField.getText();
    }

    public void setUsernameText(String usernameText) {
        usernameTextField.setText(usernameText);
    }

    public String getPasswordText() {
        return new String(passwordField.getPassword());
    }

    public void setPasswordText(String passwordText) {
        passwordField.setText(passwordText);
    }
}
