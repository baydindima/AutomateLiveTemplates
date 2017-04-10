package edu.jetbrains.plugin.lt.newui;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBPanel;
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
    private EditorTextField textField;

    private List<Template> templateList;

    private boolean[] selectedTemplateList;
    private FileType fileType;
    private int curIndex;

    public ChooseImportantTemplates(Project project, FileType fileType, List<Template> templateList) {
        this.project = project;
        this.templateList = templateList;
        this.fileType = fileType;
        selectedTemplateList = new boolean[templateList.size()];

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        fileTypeLabel.setText("File type: " + fileType.getName());

        bodyPane.add(textField, BorderLayout.CENTER);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        selectedTemplateList = new boolean[0];
        dispose();
    }

    public List<Template> showDialog() {
        pack();
        setVisible(true);
        List<Template> result = new ArrayList<>();
        for (int i = 0; i < selectedTemplateList.length; i++) {
            if (selectedTemplateList[i]) {
                result.add(templateList.get(i));
            }
        }
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
}
