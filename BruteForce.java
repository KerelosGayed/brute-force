import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BruteForce {
    /**
     * Tries out each password in a given array
     */
    static String simpleDictAtk(String[] passwords, Function<String, Boolean> passwordAttempter, int start, int end) {
        for (int i = start; i < end; i++) {
            if (passwordAttempter.apply(passwords[i])) {
                System.out.println("Woohoo! Password is: " + passwords[i]);
                return passwords[i];
            }
        }
        return null;
    }

    /**
     * Recursively generates and tries all possible passwords of the given length.
     * Now also prints progress every 1000 attempts, showing the total number of possibilities.
     */
    static String charCombos(char[] characters, String prefix, int length, 
                             Function<String, Boolean> passwordAttempter, AtomicInteger attempts, 
                             int totalCombinations, long startTime) {
        if (length == 0) {
            int attemptCount = attempts.incrementAndGet();
            boolean result = passwordAttempter.apply(prefix);

            // Print progress every 1000 attempts
            if (attemptCount % 1000 == 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                double progress = (100.0 * attemptCount) / totalCombinations;
                System.out.printf("Attempts: %d / %d (%.2f%%) | Time elapsed: %.2f seconds%n",
                        attemptCount, totalCombinations, progress, elapsedTime / 1000.0);
            }

            return result ? prefix : null;
        }

        for (char c : characters) {
            String found = charCombos(characters, prefix + c, length-1, passwordAttempter, attempts, totalCombinations, startTime);
            if (found != null) return found; // Stop recursion if password is found
        }
        return null;
    }
}
