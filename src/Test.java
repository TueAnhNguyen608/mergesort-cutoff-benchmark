import java.util.*;

public class Test {
    public static void main(String[] args) {
        Random r = new Random(1);
        int[] cutoffs = {1, 2, 7, 10, 50, 100000};
        int[] sizes = {0, 1, 2, 3, 10, 11, 100, 1000, 12345};
        for (int c : cutoffs)
            for (int n : sizes)
                for (int rep = 0; rep < 50; rep++) {
                    int[] a = new int[n];
                    for (int i = 0; i < n; i++)
                        a[i] = (rep % 2 == 0) ? r.nextInt() : r.nextInt(5); // rep lẻ: nhiều số trùng
                    int[] expected = a.clone();
                    Arrays.sort(expected);
                    MergeSort.sort(a, c);
                    if (!Arrays.equals(a, expected)) {
                        System.out.println("SAI: cutoff=" + c + " n=" + n);
                        return;
                    }
                }
        System.out.println("Tất cả test đều đúng.");
    }
}