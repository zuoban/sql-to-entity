package cn.leftsite.sqltoentity.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class SampleDialogWrapper extends DialogWrapper {
    private final String content;

    public SampleDialogWrapper(String content) {
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
        //textArea.setPreferredSize(new Dimension(300, 400));
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