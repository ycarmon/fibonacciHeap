
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class FibonacciHeapTest{
    @Test
    void TestisEmpty() {
        FibonacciHeap fibTree = new FibonacciHeap();
        assertTrue( fibTree.isEmpty());
    }

}
