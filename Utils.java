import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class Utils {
    static int binarySearch(long[] sins, long target) {
        int left = 0, right = sins.length -1;
        boolean found = false;
        int steps = 0;
        int targetPos = 0;
        while (!found && left <= right) {
            targetPos = (right + left)/2;
            if (sins[targetPos] == target) {
                found =  true;
                return targetPos;
            } else if (sins[targetPos] < target) {
                left = targetPos;
                steps ++;
            } else if (sins[targetPos] > target) {
                right = targetPos;
                steps ++;
            } if (steps > 2000) {
                found = false;
                break;
            }
        }
        if (found) {
            return targetPos;
        } else {
            return -1;
        }
    }
    static long[] readLongs(String filename) throws FileNotFoundException {
        Scanner filesIn = new Scanner(new File(filename));
        int count = Integer.parseInt(filesIn.nextLine());
        long[] array = new long[count];
        for(int i = 0; i < count; i++) {
            array[i] = Long.parseLong(filesIn.nextLine());
        }
        filesIn.close();
        return array;
    }
    static String[] readStrings(String filename) throws FileNotFoundException {
        Scanner stringsIn = new Scanner(new File(filename));
        int count = Integer.parseInt(stringsIn.nextLine());
        String[] strings = new String[count];
        for(int i = 0; i < count; i++) {
            strings[i] = stringsIn.nextLine();
        }
        return strings;
    }
    /**
     * Prompts user for an int between specified low (inclusive) and high (inclusive)
     * @param targetSin Scanner for terminal input
     * @param low Lowest allowed value
     * @param high Highest allowed value
     * @return Valid user input
     */


    /*
     * Wrapper for System.out.println;
     */
    static <T> void prn(T thingToPrint) {
        System.out.println(thingToPrint);
    }


    /*
     * Wrapper for System.out.print;
     */
    static <T> void prt(T thingToPrint) {
        System.out.print(thingToPrint);
    }


    
}