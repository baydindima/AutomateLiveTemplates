package homework.torrent;

import homework.torrent.app.TorrentTrackerImpl;
import homework.torrent.exception.InvalidServerState;
import homework.torrent.network.server.TorrentServer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Main class for server.
 */
@Slf4j
public class ServerMain {

    /**
     * Start server.
     *
     * @param args server params
     */
    public static void main(String[] args) {
        TorrentTrackerImpl tracker = null;
        try {
            tracker = TorrentTrackerImpl.createOrGetFromDisk();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error during load state from disk.");
            log.error("Error during load state from disk.");
            System.exit(1);
        }
        TorrentServer torrentServer = new TorrentServer(tracker);
        try {
            torrentServer.start();

            Scanner scanner = new Scanner(new BufferedInputStream(System.in));
            while (true) {
                String command = scanner.nextLine();
                if (Objects.equals(command, "exit")) {
                    break;
                }
            }

            torrentServer.close();
        } catch (IOException | InvalidServerState e) {
            System.out.println("Exception during server start.");
            log.error("Exception during server start.", e);
        }

        TorrentTrackerImpl finalTracker = tracker;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                finalTracker.saveOnDisk();
            } catch (IOException e) {
                log.error("Exception during save state on disk.", e);
            }
        }));
    }


}
