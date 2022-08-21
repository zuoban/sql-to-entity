package cn.leftsite.sqltoentity.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ShowEntityDialog extends DialogWrapper {
    private final String content;

    public ShowEntityDialog(String content) {
        super(true); // use current window as parent
        this.content = content;
        setTitle("Properties");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(content);
        dialogPanel.add(textArea, BorderLayout.CENTER);
        return dialogPanel;
    }

    @Override
    protected void doOKAction() {
        StringSelection selection = new StringSelection(content);
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(selection, selection);
        super.doOKAction();
    }
}