package edu.jetbrains.plugin.lt.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import edu.jetbrains.plugin.lt.finder.Modes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ModesDialog extends DialogWrapper {
    private Modes.ModeHolder modeHolder;

    protected ModesDialog(Project project, Modes.ModeHolder modeHolder) {
        super(project);
        this.modeHolder = modeHolder;
        setTitle("Modes");
        init();
    }

    public static Modes.Name showDialogAndGetMode(Project project) {
        Modes.ModeHolder modeHolder = new Modes.ModeHolder(Modes.Name.CONTROL_STRUCTURES_FINDER);
        ModesDialog modesDialog = new ModesDialog(project, modeHolder);
        modesDialog.show();
        if (modesDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) return modeHolder.getMode();
        return null;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel modesPanel = new JPanel(new GridLayout(1 + Modes.Name.values().length, 1));
        JLabel jLabel = new JBLabel("Find:");
        modesPanel.add(jLabel);
        ButtonGroup buttonGroup = new ButtonGroup();
        for (Modes.Name mode : Modes.Name.values()) {
            JRadioButton jRadioButton = new JBRadioButton(Modes.getText(mode));
            jRadioButton.addActionListener(e -> modeHolder.setMode(mode));
            buttonGroup.add(jRadioButton);
            modesPanel.add(jRadioButton);
        }

        JPanel centerPanel = new JBPanel<>(new BorderLayout());
        centerPanel.add(modesPanel, BorderLayout.NORTH);
        return centerPanel;
    }
}
