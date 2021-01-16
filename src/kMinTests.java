public class kMinTests {

    public static void main(String[] args) {
        for (int i = 1; i <= 15; i++) {
            System.out.println(i);
            System.out.println(TestKMin(getBinomialTreeOfRank(i)));
        }
    }

    public static boolean TestKMin(FibonacciHeap H) {
        for (int i = 1; i < H.size(); i++) {
            if (!containsAll(H.kMin(H, i), i)) {
                return false;
            }
        }

        return true;
    }

    private static boolean containsAll(int[] a, int num) {
        for (int i = 1; i <= num; i++) {
            if (a[i-1] != i) {
                return false;
            }
        }

        return true;
    }

    private static FibonacciHeap getBinomialTreeOfRank(int rank) {
        int size = (int) Math.pow(2, rank);
        FibonacciHeap h = new FibonacciHeap();

        for (int i = 0; i <= size; i++) {
            h.insert(i);
        }

        h.deleteMin();

        return h;
    }
}
