package edu.jetbrains.plugin.lt.newui;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import edu.jetbrains.plugin.lt.finder.common.Template;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ChooseImportantTemplates extends JDialog {
    private Project project;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel bodyPane;

    private JButton nextButton;
    private JButton prevButton;
    private JCheckBox isImportantCheckBox;
    private JLabel fileTypeLabel;
    private JLabel occurenceCountLabel;
    private JLabel curPosLabel;
    private EditorTextField textField;

    private List<Template> templateList;

    private boolean[] selectedTemplateList;
    private FileType fileType;
    private int curIndex;
    private List<Template> result = new ArrayList<>();


    public ChooseImportantTemplates(Project project, FileType fileType, List<Template> templateList) {
        this.project = project;
        this.templateList = templateList;
        this.fileType = fileType;
        $$$setupUI$$$();
        selectedTemplateList = new boolean[templateList.size()];

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        fileTypeLabel.setText("File type: " + fileType.getName());

        bodyPane.add(textField, BorderLayout.CENTER);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        setNewTemplate(curIndex);
    }

    private void updateSelectedList() {
        selectedTemplateList[curIndex] = isImportantCheckBox.isSelected();
    }

    private void nextTemplate() {
        updateSelectedList();
        setNewTemplate(++curIndex);
    }

    private void prevTemplate() {
        updateSelectedList();
        setNewTemplate(--curIndex);
    }

    private void setNewTemplate(int templateIndex) {
        if (templateIndex < 0 || templateIndex >= templateList.size())
            throw new NoSuchTemplateException("No such template index : " + templateIndex + " from " + templateList.size());

        nextButton.setEnabled(templateIndex < templateList.size() - 1);
        prevButton.setEnabled(templateIndex > 0);

        Template template = templateList.get(templateIndex);

        textField.setText(template.text());
        occurenceCountLabel.setText("Occurrence count: " + template.templateStatistic().occurrenceCount());
        isImportantCheckBox.setSelected(selectedTemplateList[templateIndex]);
        curPosLabel.setText(String.format("%d/%d", curIndex + 1, templateList.size()));
    }

    private void onOK() {
        for (int i = 0; i < selectedTemplateList.length; i++) {
            if (selectedTemplateList[i]) {
                result.add(templateList.get(i));
            }
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public List<Template> showDialog() {
        pack();
        setVisible(true);
        return result;
    }

    private void createUIComponents() {
        nextButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("png/arrow_right.png")));
        nextButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (nextButton.isEnabled()) {
                    nextTemplate();
                }
                super.mouseClicked(e);
            }
        });
        prevButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("png/arrow_left.png")));
        prevButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (prevButton.isEnabled()) {
                    prevTemplate();
                }
                super.mouseClicked(e);
            }
        });
        bodyPane = new JBPanel<>(new BorderLayout());
        textField = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, fileType, true, false);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 4, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contentPane.add(bodyPane, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(600, 400), null, null, 0, false));
        prevButton.setText("");
        contentPane.add(prevButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextButton.setText("");
        contentPane.add(nextButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isImportantCheckBox = new JCheckBox();
        isImportantCheckBox.setText("is important");
        contentPane.add(isImportantCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileTypeLabel = new JLabel();
        fileTypeLabel.setText("Label");
        contentPane.add(fileTypeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        occurenceCountLabel = new JLabel();
        occurenceCountLabel.setText("Label");
        contentPane.add(occurenceCountLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        curPosLabel = new JLabel();
        curPosLabel.setText("Label");
        contentPane.add(curPosLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
