package edu.jetbrains.plugin.lt.newui;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
import edu.jetbrains.plugin.lt.finder.template.TemplateNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Dmitriy Baidin.
 */
public class TemplateNodeStatisticDialog extends DialogWrapper {
    private static final String EMPTY_STRING = "";

    @Nullable
    private final Project project;
    @NotNull
    private final TemplateNode templateNode;

    public TemplateNodeStatisticDialog(@Nullable Project project,
                                       @NotNull TemplateNode templateNode) {
        super(project);
        this.project = project;
        this.templateNode = templateNode;
        setTitle("Statistic for " + templateNode.toString());
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel jPanel = new JBPanel<>(new BorderLayout());
        jPanel.setMinimumSize(new Dimension(750, 450));
        EditorTextField editorTextField = new EditorTextField(
                EditorFactory.getInstance().createDocument(EMPTY_STRING),
                project,
                FileTypes.UNKNOWN,
                true,
                false
        );
        editorTextField.setText(templateNode.getStatisticsString());
        jPanel.add(editorTextField);
        return jPanel;
    }
}
