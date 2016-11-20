package edu.jetbrains.plugin.lt.newui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import edu.jetbrains.plugin.lt.finder.template.TemplateNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by Dmitriy Baidin.
 */
public class TemplateRootsDialog extends DialogWrapper {
    @Nullable
    private final Project project;
    @NotNull
    private final List<TemplateNode> statistics;

    public TemplateRootsDialog(@Nullable Project project,
                               @NotNull List<TemplateNode> statistics) {
        super(project);
        this.project = project;
        this.statistics = statistics;
        setTitle("Possible Template Roots");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel jPanel = new JBPanel<>(new BorderLayout());
        jPanel.setMinimumSize(new Dimension(750, 450));

        JBList jbList = new JBList(statistics);
        jPanel.add(jbList);

        jbList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    System.out.println("Double clicked!");
                    new TemplateNodeStatisticDialog(project,
                            statistics.get(jbList.locationToIndex(e.getPoint()))
                    ).show();
                }
                super.mouseClicked(e);
            }
        });

        return jPanel;
    }
}
