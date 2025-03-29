import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * An automation tool built in java.
 * <p>
 * This Java program automates the process of creating Git commits and pushing them to a remote repository.
 * It writes timestamps to a file, commits the changes, and pushes them periodically. It retries the push
 * in case of network failures. The program uses multithreading to handle Git process output.
 * </p>
 *
 * <h2>🛠️ Features:</h2>
 * <ul>
 *     <li>Generates and writes timestamps to a file</li>
 *     <li>Executes Git commands (add, commit, push)</li>
 *     <li>Retries Git push operations in case of failure</li>
 *     <li>Logs output and errors to a log file</li>
 *     <li>Multithreaded log handling</li>
 * </ul>
 *
 * @author Anurag Zete
 * @version 1.0
 * @since 2025-02-14
 */
public class StreakBooster {

    /** Logger for logging application events. */
    private static final Logger logger = Logger.getLogger(StreakBooster.class.getName());

    /** Path to the log file where application events are recorded. */
    private static final String LOG_FILE_PATH = "logRecords.log";

    /** Path to the file where commit timestamps are saved. */
    private static final String TIMESTAMP_FILE_PATH = "records.txt";

    /** Maximum number of retries for Git push operations. */
    private static final int MAX_RETRIES = 2;

    /** Thread pool executor to handle concurrent process output logging. */
    private static final ExecutorService processOutputExecutor = Executors.newFixedThreadPool(2);

    /**
     * Default constructor.
     * Initializes the StreakBooster class.
     */
    public StreakBooster() { }

    /**
     * The main method that runs the StreakBooster.
     * It performs the following tasks:
     * <ul>
     *     <li>Configures the logger</li>
     *     <li>Writes timestamp to a file</li>
     *     <li>Executes Git add and commit commands</li>
     *     <li>Checks for internet connectivity</li>
     *     <li>Pushes changes with retries</li>
     * </ul>
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            configureLogger();
            writeTimestampToFile();
            executeGitCommands();

            if (isInternetConnected()) {
                boolean pushSuccess = executeGitPushWithRetries();
                if (pushSuccess) {
                    logger.info("Git push successful.");
                }
            } else {
                logger.warning("No internet connection. Skipping Git push.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred", e);
        } finally {
            shutdown();
        }
    }

    /**
     * Configures the logger with a file handler and simple formatter.
     * The logs are saved to `logRecords.log`.
     *
     * @throws IOException if the log file cannot be created or accessed.
     */
    private static void configureLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(LOG_FILE_PATH, true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);
    }

    /**
     * Writes the current timestamp to the `records.txt` file.
     *
     * @throws IOException if the file cannot be written.
     */
    private static void writeTimestampToFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (FileWriter writer = new FileWriter(TIMESTAMP_FILE_PATH, true)) {
            writer.write(timestamp + "\n");
        }
        logger.info("Timestamp written to file: " + timestamp);
    }

    /**
     * Executes Git add and commit commands.
     * <ul>
     *     <li>Adds `records.txt` and `logRecords.log` to the staging area</li>
     *     <li>Commits the changes with an auto-generated message</li>
     * </ul>
     *
     * @throws IOException          if the Git commands fail to execute.
     * @throws InterruptedException if the process is interrupted.
     */
    private static void executeGitCommands() throws IOException, InterruptedException {
        executeGitCommand("git", "add", TIMESTAMP_FILE_PATH, LOG_FILE_PATH);
        executeGitCommand("git", "commit", "-m", "Auto commit: Update timestamp and logs");
    }

    /**
     * Checks if the system is connected to the internet by sending a HEAD request to GitHub.
     *
     * @return {@code true} if connected, {@code false} otherwise.
     */
    private static boolean isInternetConnected() {
        try {
            URL url = URI.create("https://www.github.com").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to check internet connectivity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes the Git push command with retries.
     * <p>
     * If the push fails, the program retries up to {@code MAX_RETRIES} times,
     * with a 60-second delay between attempts.
     * </p>
     *
     * @return {@code true} if the push is successful, {@code false} otherwise.
     */
    private static boolean executeGitPushWithRetries() {
        for (int retry = 0; retry <= MAX_RETRIES; retry++) {
            try {
                executeGitCommand("git", "push");
                return true;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Git push failed (Attempt " + (retry + 1) + "): " + e.getMessage());
                if (retry < MAX_RETRIES) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        logger.log(Level.SEVERE, "Retry interrupted", ex);
                        break;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Executes a Git command using {@code ProcessBuilder}.
     * It logs the output and errors concurrently using multiple threads.
     *
     * @param command The Git command and its arguments.
     * @throws IOException          if the command execution fails.
     * @throws InterruptedException if the process is interrupted.
     */
    private static void executeGitCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();
        processOutputExecutor.submit(() -> logStream(process.getInputStream(), Level.INFO));
        processOutputExecutor.submit(() -> logStream(process.getErrorStream(), Level.WARNING));

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Git command failed with exit code: " + exitCode);
        }
    }

    /**
     * Logs the output stream of a process.
     * Uses a thread pool to handle multiple outputs concurrently.
     *
     * @param inputStream The stream to read.
     * @param level       The log level (INFO or WARNING).
     */
    private static void logStream(InputStream inputStream, Level level) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.log(level, line);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading process output: " + e.getMessage());
        }
    }

    /**
     * Shuts down the executor service and closes all logger handlers.
     * Ensures proper cleanup of resources.
     */
    private static void shutdown() {
        logger.info("Shutting down resources...");
        processOutputExecutor.shutdown();
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
        logger.info("Shutdown completed successfully.");
    }
}
