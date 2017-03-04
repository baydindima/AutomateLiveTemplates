package edu.jetbrains.plugin.lt.newui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChooseFileTypeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel bodyPane;
    private JCheckBoxList jCheckBoxList;
    private List<FileType> fileTypes = new ArrayList<>();
    private List<FileType> selectedFileTypes;

    public ChooseFileTypeDialog(Map<FileType, Integer> fileTypeToCount) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();
        jCheckBoxList = new JCheckBoxList(listModel);

        for (Map.Entry<FileType, Integer> fileTypeWithCount : fileTypeToCount.entrySet()) {
            JCheckBox checkBox = new JCheckBox(
                    String.format("%s (%d)",
                            fileTypeWithCount.getKey().getName(),
                            fileTypeWithCount.getValue())
            );
            listModel.addElement(checkBox);
            fileTypes.add(fileTypeWithCount.getKey());
        }

        bodyPane.add(jCheckBoxList, new GridConstraints());

        contentPane.registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    private void onOK() {
        selectedFileTypes = new ArrayList<>();
        ListModel<JCheckBox> model = jCheckBoxList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            JCheckBox checkBox = model.getElementAt(i);
            if (checkBox.isSelected()) {
                selectedFileTypes.add(fileTypes.get(i));
            }
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }


    public List<FileType> getSelectedFileTypes() {
        return selectedFileTypes;
    }

    public List<FileType> showDialog() {
        pack();
        setVisible(true);
        return selectedFileTypes;
    }
}
