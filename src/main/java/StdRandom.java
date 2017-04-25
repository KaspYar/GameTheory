import java.util.Random;

public final class StdRandom {

    private static Random random = new Random();

    public static double uniform() {
        return random.nextDouble();
    }

    public static int uniform(int n) {
        if (n <= 0) throw new IllegalArgumentException("argument must be positive");
        return random.nextInt(n);
    }

    public static double uniform(double a, double b) {
        if (!(a < b)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + "]");
        }
        return a + uniform() * (b-a);
    }
}