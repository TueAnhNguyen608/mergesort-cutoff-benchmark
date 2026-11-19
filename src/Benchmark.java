import java.io.*;
import java.util.*;

public class Benchmark {
    static final int[] CUTOFFS = {1,2,3,4,5,6,7,8,9,10,11,12,14,16,18,20,24,28,32,40,50,75,100,150,200,300};
    static final int[] SIZES   = {10_000, 100_000, 1_000_000};
    static final int TRIALS = 30, WARMUP = 10












            ;

    public static void main(String[] args) throws IOException {
        try (PrintWriter out = new PrintWriter("data/results.csv")) {
            out.println("type,n,trial,cutoff,time_ns");
            for (String type : new String[]{"random", "nearly_sorted", "reversed", "duplicates"}) {
                for (int n : SIZES) {
                    // warm-up
                    for (int w = 0; w < WARMUP; w++)
                        for (int c : CUTOFFS) MergeSort.sort(gen(type, n, 999 + w), c);

                    for (int t = 0; t < TRIALS; t++) {
                        int[] base = gen(type, n, t);               // seed = t, tái lập được
                        List<Integer> order = new ArrayList<>();
                        for (int c : CUTOFFS) order.add(c);
                        Collections.shuffle(order, new Random(t));   // xáo thứ tự cutoff
                        for (int c : order) {
                            int[] a = base.clone();
                            long t0 = System.nanoTime();
                            MergeSort.sort(a, c);
                            long t1 = System.nanoTime();
                            out.printf("%s,%d,%d,%d,%d%n", type, n, t, c, t1 - t0);
                        }
                    }
                    System.out.println("Xong " + type + " n=" + n);
                }
            }
        }
    }

    static int[] gen(String type, int n, long seed) {
        Random r = new Random(seed);
        int[] a = new int[n];
        switch (type) {
            case "random":        for (int i = 0; i < n; i++) a[i] = r.nextInt(); break;
            case "nearly_sorted": for (int i = 0; i < n; i++) a[i] = i;
                for (int k = 0; k < n / 100; k++) {   // đảo 1% cặp
                    int i = r.nextInt(n), j = r.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t; } break;
            case "reversed":      for (int i = 0; i < n; i++) a[i] = n - i; break;
            case "duplicates":    for (int i = 0; i < n; i++) a[i] = r.nextInt(100); break;
        }
        return a;
    }
}