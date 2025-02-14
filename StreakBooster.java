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


public class StreakBooster {

    private static final Logger logger = Logger.getLogger(StreakBooster.class.getName());
    private static final String LOG_FILE_PATH = "logRecords.log";
    private static final String TIMESTAMP_FILE_PATH = "records.txt";
    private static final int MAX_RETRIES = 2;

    private static final ExecutorService processOutputExecutor = Executors.newFixedThreadPool(2);

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

    private static void configureLogger() throws IOException {
        FileHandler fileHandler = new FileHandler(LOG_FILE_PATH, true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);
    }

    private static void writeTimestampToFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (FileWriter writer = new FileWriter(TIMESTAMP_FILE_PATH, true)) {
            writer.write(timestamp + "\n");
        }
        logger.info("Timestamp written to file: " + timestamp);
    }

    private static void executeGitCommands() throws IOException, InterruptedException {
        executeGitCommand("git", "add", TIMESTAMP_FILE_PATH, LOG_FILE_PATH);
        executeGitCommand("git", "commit", "-m", "Auto commit: Update timestamp and logs");
    }

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

    private static void shutdown() {
        logger.info("Shutting down resources...");
        processOutputExecutor.shutdown();
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
        logger.info("Shutdown completed successfully.");
    }
}
