import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.concurrent.ExecutorService;
import java.awt.Toolkit;

class Main {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose attack method: (1) Dictionary Attack, (2) Brute Force Attack");
        int choice = scanner.nextInt();

        String sevenZexe = "./bin/7zr_win.exe"; // Change if using Linux/Mac.
        String secretFile = "secrets/2.7z"; // File to extract.
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis(); // Start tracking time

        Function<String, Boolean> callback = password -> {
            if (found.get()) return true;
            boolean result = usePassword(sevenZexe, secretFile, password);
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
            int chunkSize = commonPasswords.length / 20;

            for (int i = 0; i < 20; i++) {
                int start = i * chunkSize;
                int end = (i == 19) ? commonPasswords.length : (i + 1) * chunkSize;
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
            int totalCombinations = (int) Math.pow(allowedCharacters.length, 6); // Total possibilities for 6-character password
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

    static Boolean usePassword(String commandExe, String filename, String password) {
        try {
            Process command = new ProcessBuilder(commandExe, "e", filename, "-y", "-p" + password).start();
            int exitCode = command.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
