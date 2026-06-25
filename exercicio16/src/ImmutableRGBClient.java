import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Client program for testing ImmutableRGB with multiple threads.
 *
 * The same ImmutableRGB instance is shared by several threads. Since the object is
 * immutable, all threads can read it and call invert() without changing the
 * original object.
 */
public class ImmutableRGBClient {

    private static final int THREADS = 10;
    private static final long TEST_DURATION_MILLIS = 3_000;

    public static void main(String[] args) throws InterruptedException {
        ImmutableRGB original = new ImmutableRGB(255, 0, 0, "red");

        int expectedOriginalRgb = RgbStates.toRgb(255, 0, 0);
        int expectedInverseRgb = RgbStates.toRgb(0, 255, 255);

        AtomicBoolean running = new AtomicBoolean(true);
        AtomicLong checks = new AtomicLong(0);
        AtomicLong originalChangedErrors = new AtomicLong(0);
        AtomicLong inverseErrors = new AtomicLong(0);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(() -> {
                while (running.get()) {
                    if (original.getRGB() != expectedOriginalRgb || !"red".equals(original.getName())) {
                        originalChangedErrors.incrementAndGet();
                    }

                    ImmutableRGB inverse = original.invert();
                    if (inverse.getRGB() != expectedInverseRgb || !"Inverse of red".equals(inverse.getName())) {
                        inverseErrors.incrementAndGet();
                    }

                    checks.incrementAndGet();
                    Thread.yield();
                }
            }, "immutable-reader-" + i);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(TEST_DURATION_MILLIS);
        running.set(false);

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("=== Teste com ImmutableRGB ===");
        System.out.println("Verificações realizadas: " + checks.get());
        System.out.println("Erros indicando mudança no objeto original: " + originalChangedErrors.get());
        System.out.println("Erros na criação do objeto invertido: " + inverseErrors.get());
        System.out.println("RGB original esperado: " + RgbStates.toHex(expectedOriginalRgb));
        System.out.println("RGB inverso esperado: " + RgbStates.toHex(expectedInverseRgb));
        System.out.println();
        System.out.println("Interpretação:");
        System.out.println("- O objeto original não muda depois de criado.");
        System.out.println("- invert() cria uma nova instância, sem alterar a instância compartilhada.");
        System.out.println("- Por isso, várias threads podem usar o mesmo objeto sem synchronized.");
    }
}
