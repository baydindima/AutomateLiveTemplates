package edu.jetbrains.plugin.lt.newui;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
import edu.jetbrains.plugin.lt.finder.stree.SimTreeStatistic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Dmitriy Baidin.
 */
public class TreeStatisticDialog extends DialogWrapper {
    private static final String EMPTY_STRING = "";
    @NotNull
    private final SimTreeStatistic treeStatistic;
    @Nullable
    private Project project;


    public TreeStatisticDialog(@Nullable Project project,
                               @NotNull FileType fileType,
                               @NotNull SimTreeStatistic treeStatistic) {
        super(project);
        this.project = project;
        setTitle("Tree statistic for " + fileType.getName());
        this.treeStatistic = treeStatistic;
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
        editorTextField.setText(treeStatistic.toString());
        jPanel.add(editorTextField);
        return jPanel;
    }
}
