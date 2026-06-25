import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Client program for testing SynchronizedRGB with multiple threads.
 *
 * The class SynchronizedRGB protects each synchronized method, but this client also
 * demonstrates an important limitation: reading getRGB() and getName() as two
 * separate method calls is a compound operation performed by the client.
 */
public class SynchronizedRGBClient {

    private static final int WRITER_THREADS = 4;
    private static final int UNSAFE_READER_THREADS = 4;
    private static final int SAFE_READER_THREADS = 2;
    private static final long TEST_DURATION_MILLIS = 3_000;

    public static void main(String[] args) throws InterruptedException {
        SynchronizedRGB color = new SynchronizedRGB(255, 0, 0, "red");

        AtomicBoolean running = new AtomicBoolean(true);
        AtomicLong writes = new AtomicLong(0);
        AtomicLong unsafeReads = new AtomicLong(0);
        AtomicLong unsafeInconsistencies = new AtomicLong(0);
        AtomicLong safeReads = new AtomicLong(0);
        AtomicLong safeInconsistencies = new AtomicLong(0);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < WRITER_THREADS; i++) {
            final int writerId = i;
            Thread writer = new Thread(() -> {
                int index = writerId;
                while (running.get()) {
                    RgbStates.State state = RgbStates.STATES[index % RgbStates.STATES.length];
                    color.set(state.red, state.green, state.blue, state.name);
                    writes.incrementAndGet();
                    index++;
                    Thread.yield();
                }
            }, "writer-" + i);
            threads.add(writer);
        }

        for (int i = 0; i < UNSAFE_READER_THREADS; i++) {
            Thread reader = new Thread(() -> {
                while (running.get()) {
                    int rgb = color.getRGB();

                    // This yield makes it easier for another thread to change the object
                    // between getRGB() and getName().
                    Thread.yield();

                    String name = color.getName();
                    unsafeReads.incrementAndGet();

                    if (!RgbStates.isConsistent(rgb, name)) {
                        unsafeInconsistencies.incrementAndGet();
                    }
                }
            }, "unsafe-reader-" + i);
            threads.add(reader);
        }

        for (int i = 0; i < SAFE_READER_THREADS; i++) {
            Thread reader = new Thread(() -> {
                while (running.get()) {
                    int rgb;
                    String name;

                    // The client synchronizes the whole compound read.
                    // This is possible because SynchronizedRGB uses the same lock: this.
                    synchronized (color) {
                        rgb = color.getRGB();
                        name = color.getName();
                    }

                    safeReads.incrementAndGet();

                    if (!RgbStates.isConsistent(rgb, name)) {
                        safeInconsistencies.incrementAndGet();
                    }
                }
            }, "safe-reader-" + i);
            threads.add(reader);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(TEST_DURATION_MILLIS);
        running.set(false);

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("=== Teste com SynchronizedRGB ===");
        System.out.println("Escritas realizadas: " + writes.get());
        System.out.println("Leituras inseguras realizadas: " + unsafeReads.get());
        System.out.println("Inconsistências em leitura insegura: " + unsafeInconsistencies.get());
        System.out.println("Leituras seguras realizadas: " + safeReads.get());
        System.out.println("Inconsistências em leitura segura: " + safeInconsistencies.get());
        System.out.println();
        System.out.println("Interpretação:");
        System.out.println("- set(), getRGB() e getName() são protegidos individualmente.");
        System.out.println("- Porém, getRGB() seguido de getName() não é uma operação atômica do cliente.");
        System.out.println("- Quando o cliente sincroniza o bloco inteiro, a leitura composta fica consistente.");
    }
}
