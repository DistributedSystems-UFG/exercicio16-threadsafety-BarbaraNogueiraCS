/**
 * Helper class used only by the client test programs.
 * It stores valid RGB/name pairs so the tests can check whether a read is consistent.
 */
final class RgbStates {

    static final class State {
        final int red;
        final int green;
        final int blue;
        final int rgb;
        final String name;

        State(int red, int green, int blue, String name) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.rgb = toRgb(red, green, blue);
            this.name = name;
        }
    }

    static final State[] STATES = {
        new State(255, 0, 0, "red"),
        new State(0, 255, 0, "green"),
        new State(0, 0, 255, "blue"),
        new State(255, 255, 255, "white"),
        new State(0, 0, 0, "black"),
        new State(255, 255, 0, "yellow"),
        new State(0, 255, 255, "cyan"),
        new State(255, 0, 255, "magenta")
    };

    private RgbStates() {
        // Utility class: it should not be instantiated.
    }

    static int toRgb(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }

    static boolean isConsistent(int rgb, String name) {
        for (State state : STATES) {
            if (state.rgb == rgb && state.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    static String toHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }
}
