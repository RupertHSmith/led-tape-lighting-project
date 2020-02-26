public class Test {
    public static void main(String args[]) {
        Test t = new Test();
        t.testComputing(2.35);
        t.testArrayImplementation(2.35);
    }

    public void testComputing(double k) {
        final double q = (k / 255d);
        final double c = (1023f / (Math.exp(k) - 1));

        System.out.println("Testing real-time computation...");
        System.out.println("TESTING FORMULA: " + (int) Math.round((Math.exp(255 * q) - 1) * c));
        long previous = System.nanoTime();

        for (int i = 0; i < 1000000; i++) {
            for (int x = 0; x < 256; x++) {
                int val = (int) Math.round((Math.exp(x * q) - 1) * c);
            }
        }

        long result = (System.nanoTime() - previous) / 1000;
        System.out.println("Took " + result + " microseconds\n\n");


    }

    public void testArrayImplementation(double k) {
        final double q = (k / 255d);
        final double c = (1023f / (Math.exp(k) - 1));

        int[] values = new int[256];
        for (int x = 0; x < 256; x++){
            values[x] = (int) Math.round((Math.exp(x * q) - 1) * c);
        }

        System.out.println("Testing array look up implementation...");
        System.out.println("TESTING FORMULA: " + values [255]);

        long previous = System.nanoTime();

        for (int i = 0; i < 1000000; i++) {
            for (int x = 0; x < 256; x++) {
                int val = values[x];
            }
        }

        long result = (System.nanoTime() - previous) / 1000;
        System.out.println("Took " + result + " microseconds");

    }

}
