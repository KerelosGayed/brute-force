import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.SevenZip.*;
import net.sf.sevenzipjbinding.impl.*;
import net.sf.sevenzipjbinding.simple.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.concurrent.ExecutorService;
import java.awt.Toolkit;

class Main {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, SevenZipNativeInitializationException {
        // Initialize SevenZipJBinding
        SevenZip.initSevenZipFromPlatformJAR();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose attack method: (1) Dictionary Attack, (2) Brute Force Attack");
        int choice = scanner.nextInt();
        String secretFile = "secrets/2.7z"; // File to extract.
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis(); // Start tracking time

        Function<String, Boolean> callback = password -> {
            if (found.get()) return true;
            boolean result = usePassword(secretFile, password);
            int attemptCount = attempts.incrementAndGet();

            // Print progress every 1000 attempts with time elapsed
            if (attemptCount % 1000 == 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Attempts: " + attemptCount + " | Time elapsed: " + (elapsedTime / 1000.0) + " seconds");
            }

            if (result) {
                found.set(true);
                System.out.println("Success! Password: " + password);
                Toolkit.getDefaultToolkit().beep(); // Play sound on success
            }
            return result;
        };

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        if (choice == 1) {
            // Dictionary Attack
            String[] commonPasswords = Utils.readStrings("dictionaries/PwnedPasswordsTop100k.txt");
            int chunkSize = commonPasswords.length / 18;

            for (int i = 0; i < 18; i++) {
                int start = i * chunkSize;
                int end = (i == 17) ? commonPasswords.length : (i + 1) * chunkSize;
                executor.submit(() -> {
                    if (!found.get() && BruteForce.simpleDictAtk(commonPasswords, callback, start, end) != null) {
                        found.set(true);
                        executor.shutdownNow();
                    }
                });
            }
        } else if (choice == 2) {
            // Brute Force Attack
            char[] allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            long totalCombinations = (long) Math.pow(allowedCharacters.length, 6); // Total possibilities for 6-character password
            for (int i = 0; i < allowedCharacters.length; i++) {
                final int index = i;
                executor.submit(() -> {
                    BruteForce.charCombos(allowedCharacters, ""+allowedCharacters[index], 5, callback, attempts, totalCombinations, startTime);
                    executor.shutdownNow();
                });
            }
        } else {
            System.out.println("Invalid choice. Exiting.");
            executor.shutdown();
        }

        executor.awaitTermination(3, java.util.concurrent.TimeUnit.DAYS);
        scanner.close();
    }

    static Boolean usePassword(String filename, String password) {
        boolean isSuccess = false;
        try {
            IInArchive archive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, new RandomAccessFileInStream(new RandomAccessFile(filename, "r")));
            ISimpleInArchive simpleInArchive = archive.getSimpleInterface();
            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                if (!item.isFolder()) {
                    ExtractOperationResult result;
                    result = item.extractSlow(data -> {
                        // Do nothing with the data
                        return data.length;
                    }, password);
                    if (result == ExtractOperationResult.OK) {
                        isSuccess = true;
                        break;
                    }
                }
            }
            archive.close();
        } catch (SevenZipException e) {
            // Incorrect password or other error
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }
}
