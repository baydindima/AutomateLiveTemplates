package homework.torrent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import homework.torrent.app.*;
import homework.torrent.exception.FileAlreadyContainsInStorage;
import homework.torrent.exception.InvalidServerState;
import homework.torrent.model.FileInfo;
import homework.torrent.network.server.ClientServer;
import homework.torrent.network.server.TorrentServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Main class for client.
 */
@Slf4j
public class ClientMain {

    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();
        try {
            new JCommander(appConfig, args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
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
            System.out.println("Invalid server address");
            log.error("Invalid server address", e);
            System.exit(1);
        }

        TorrentClientStorageImpl storage = null;
        try {
            storage = TorrentClientStorageImpl.createOrGetFromDisk();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception during load storage from disk.");
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

            startREPL(serverAddress, storage);

            clientServer.close();
            clientScheduledUpdater.stop();
        } catch (IOException | InvalidServerState e) {
            System.out.println("Exception during server start.");
            log.error("Exception during server start.", e);
        }

        TorrentClientStorageImpl finalStorage = storage;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                finalStorage.saveOnDisk();
            } catch (IOException e) {
                log.error("Exception during save file to disk", e);
            }
        }
        ));
        System.exit(0);
    }

    private static void startREPL(@NotNull
                                  final InetSocketAddress serverAddress,
                                  @NotNull
                                  final TorrentClientStorageInternal storage) {
        TorrentClient torrentClient = new TorrentClientImpl(
                serverAddress,
                storage
        );
        Scanner scanner = new Scanner(new BufferedInputStream(System.in));
        while (true) {
            String command = scanner.nextLine();
            String[] args = command.split(" ");
            switch (args[0]) {
                case "download":
                    new DownloadCommand().processCommand(torrentClient, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "upload":
                    new UploadCommand().processCommand(torrentClient, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "list":
                    new ListCommand().processCommand(torrentClient);
                    break;
                case "exit":
                    return;
                default:
                    System.out.println("Invalid command\n:" +
                            "download for file load\n" +
                            "upload for file upload\n" +
                            "list for files list\n" +
                            "exit for exit");
            }
        }
    }

    @Getter
    private final static class AppConfig {
        @Parameter(names = "-hostaddress", description = "Address of server", required = true)
        private String serverAddress;
        @Parameter(names = "-listeningport", description = "Listening port", required = true)
        private int listeningPort;
    }

    @Getter
    private final static class UploadCommand {
        @Parameter(names = {"-source", "-s"}, description = "Path to uploading file", required = true)
        private String path;

        void processCommand(@NotNull final TorrentClient torrentClient, String[] args) {
            try {
                new JCommander(this, args);
                FileInfo fileInfo = torrentClient.addFileToTorrent(Paths.get(path)).get();
                System.out.println("File uploaded " + fileInfo);
            } catch (ParameterException | InterruptedException | ExecutionException | NoSuchFileException e) {
                log.error("Exception during download file.", e);
            }
        }
    }

    @Getter
    private final static class DownloadCommand {
        @Parameter(names = {"-dest", "-d"}, description = "Path to downloading file", required = true)
        private String path;
        @Parameter(names = "-id", description = "File id", required = true)
        private long fileId;

        void processCommand(@NotNull final TorrentClient torrentClient, String[] args) {
            try {
                new JCommander(this, args);
                List<FileInfo> fileInfos = torrentClient.getAvailableFiles().get();
                Optional<FileInfo> info = fileInfos.stream()
                        .filter(fileInfo -> fileInfo.getId() == fileId).findFirst();
                if (info.isPresent()) {
                    torrentClient.downloadFile(Paths.get(path), info.get());
                } else {
                    log.error("No such available file.");
                }
            } catch (ParameterException | FileAlreadyContainsInStorage | IOException | InterruptedException | ExecutionException e) {
                log.error("Exception during download file.", e);
            }
        }
    }

    private final static class ListCommand {
        void processCommand(@NotNull final TorrentClient torrentClient) {
            try {
                List<FileInfo> fileInfos = torrentClient.getAvailableFiles().get();
                fileInfos.forEach(System.out::println);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

}
