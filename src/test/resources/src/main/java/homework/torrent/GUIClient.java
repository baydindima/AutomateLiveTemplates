package homework.torrent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import homework.torrent.app.ClientScheduledUpdater;
import homework.torrent.app.TorrentClient;
import homework.torrent.app.TorrentClientImpl;
import homework.torrent.app.TorrentClientStorageImpl;
import homework.torrent.exception.FileAlreadyContainsInStorage;
import homework.torrent.exception.InvalidServerState;
import homework.torrent.model.FileInfo;
import homework.torrent.model.ProgressListener;
import homework.torrent.network.server.ClientServer;
import homework.torrent.network.server.TorrentServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * GUI client for torrent.
 */
@Slf4j
public class GUIClient extends JFrame {
    private static final String COLUMN_NAME_NAME = "Name";
    private static final String COLUMN_NAME_SIZE = "Size";
    private static final String COLUMN_NAME_PROGRESS = "Progress";
    private static final int COLUMN_ID_PROGRESS = 2;
    @NotNull
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private JList<FileInfo> availableFiles;
    private JTable loadingFiles;
    private JPanel jPanel;
    private JScrollPane jScrollPane1;
    private JScrollPane JScrollPane2;
    private JButton uploadButton;


    public GUIClient(@NotNull
                     final InetSocketAddress serverAddress,
                     @NotNull final TorrentClientStorageImpl storage) {
        EventQueue.invokeLater(() -> {

            TorrentClient torrentClient = new TorrentClientImpl(
                    serverAddress,
                    storage
            );

            List<FileInfo> fileInfos = new ArrayList<>();
            try {
                fileInfos.addAll(torrentClient.getAvailableFiles().get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Exception during get file from server.", e);
            }
            DefaultListModel<FileInfo> model = new DefaultListModel<>();
            fileInfos.forEach(model::addElement);
            availableFiles.setModel(model);

            availableFiles.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        FileInfo fileInfo = model.getElementAt(availableFiles.locationToIndex(e.getPoint()));
                        File destination = getFileFromUser();
                        if (destination != null) {
                            try {
                                ProgressListener progressListener = torrentClient.downloadFile(destination.toPath(), fileInfo);
                                int rowNum = tableModel.getRowCount();
                                tableModel.addRow(new Object[]{fileInfo.getName(), fileInfo.getSize(), 0});

                                new Thread(() -> {
                                    while (true) {
                                        int currentProgress = progressListener.getCurrentProgress();
                                        tableModel.setValueAt(currentProgress, rowNum, COLUMN_ID_PROGRESS);
                                        if (currentProgress == 100) {
                                            break;
                                        }
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }).start();

                            } catch (IOException | FileAlreadyContainsInStorage e1) {
                                log.error("Exception during file loading!", e1);
                                showErrorMessageDialog("Exception during file loading!");
                            }
                        }
                    }
                    super.mouseClicked(e);
                }
            });


            loadingFiles.setModel(tableModel);
            tableModel.addColumn(COLUMN_NAME_NAME);
            tableModel.addColumn(COLUMN_NAME_SIZE);
            tableModel.addColumn(COLUMN_NAME_PROGRESS);

            loadingFiles.getColumn("Progress").setCellRenderer(new ProgressCellRender());


            setContentPane(jPanel);
            uploadButton.addActionListener(e -> {
                File selectedFile = getFileFromUser();
                if (selectedFile != null) {
                    try {
                        Future<FileInfo> fileInfoFuture = torrentClient.addFileToTorrent(selectedFile.toPath());
                        model.addElement(fileInfoFuture.get());
                    } catch (NoSuchFileException e1) {
                        showErrorMessageDialog("No such file!");
                        log.error("No such file!", e);
                    } catch (InterruptedException | ExecutionException e1) {
                        showErrorMessageDialog("Exception during adding!");
                        log.error("Exception during adding!", e);
                    }
                }
            });

            pack();
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        });
    }

    public static void showErrorMessageDialog(@NotNull final String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();
        try {
            new JCommander(appConfig, args);
        } catch (ParameterException e) {
            showErrorMessageDialog("Invalid params! " + e.getMessage());
            log.error("Invalid params!", e);
            System.exit(1);
        }

        InetSocketAddress serverAddress = null;
        try {
            serverAddress = new InetSocketAddress(
                    InetAddress.getByName(appConfig.getServerAddress()),
                    TorrentServer.TORRENT_PORT
            );
        } catch (UnknownHostException e) {
            showErrorMessageDialog("Invalid server address");
            log.error("Invalid server address", e);
            System.exit(1);
        }

        TorrentClientStorageImpl storage = null;
        try {
            storage = TorrentClientStorageImpl.createOrGetFromDisk();
        } catch (IOException | ClassNotFoundException e) {
            showErrorMessageDialog("Exception during load storage from disk.");
            log.error("Exception during load storage from disk.", e);
            System.exit(1);
        }

        ClientScheduledUpdater clientScheduledUpdater = new ClientScheduledUpdater(
                storage,
                serverAddress,
                appConfig.getListeningPort()
        );
        ClientServer clientServer = new ClientServer(
                appConfig.getListeningPort(),
                storage
        );

        try {
            clientServer.start();
            clientScheduledUpdater.start();
            new GUIClient(serverAddress, storage);
        } catch (IOException | InvalidServerState e) {
            System.out.println("Exception during server start.");
            log.error("Exception during server start.", e);
        }

        TorrentClientStorageImpl finalStorage = storage;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                clientServer.close();
                clientScheduledUpdater.stop();
                finalStorage.saveOnDisk();
            } catch (IOException e) {
                log.error("Exception during save file to disk", e);
            }
        }
        ));
    }

    @Nullable
    private File getFileFromUser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(jPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    @Getter
    private final static class AppConfig {
        @Parameter(names = "-hostaddress", description = "Address of server", required = true)
        private String serverAddress;
        @Parameter(names = "-listeningport", description = "Listening port", required = true)
        private int listeningPort;
    }

    public static class ProgressCellRender extends JProgressBar implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setValue((int) value);
            return this;
        }
    }
}
