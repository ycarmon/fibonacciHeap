
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class FibonacciHeapTest {
    @Test
    void TestisEmpty() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        assertTrue(fibHeap.isEmpty());
    }

    @Test
    void TestisEmptyAfterInsert() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        fibHeap.insert(1);
        assertFalse(fibHeap.isEmpty());
    }

    @Test
    void TestInsertThousandRoots() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        for (int i = 0; i < 1000; i++) {
            fibHeap.insert(i);
            assertEquals(i + 1, fibHeap.size());
        }
    }

    @Test
    void TestMeldTwoEmptyTrees() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        FibonacciHeap fibHeap2 = new FibonacciHeap();
        fibHeap.meld(fibHeap2);
        assertEquals(0, fibHeap.size());
    }

    @Test
    void TestMeldWithEmpty() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        fibHeap.insert(444);
        FibonacciHeap fibHeap2 = new FibonacciHeap();
        fibHeap.meld(fibHeap2);
        assertEquals(1, fibHeap.size());
    }

    @Test
    void TestSmallMeld() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        fibHeap.insert(444);
        FibonacciHeap fibHeap2 = new FibonacciHeap();
        fibHeap2.insert(10);
        fibHeap.meld(fibHeap2);
        assertEquals(2, fibHeap.size());
        assertEquals(fibHeap2.findMin(), fibHeap.findMin());
    }

    @Test
    void TestDeleteMin() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        for (int i=0; i< 10; i++)
            fibHeap.insert(i);
        assertEquals(10, fibHeap.size());
        fibHeap.deleteMin();
        assertEquals(9, fibHeap.size());
        assertEquals(1, fibHeap.findMin().getKey());
    }

    @Test
    void TestDeleteMinToEmpty() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        fibHeap.insert(0);
        assertEquals(1, fibHeap.size());
        fibHeap.deleteMin();
        assertEquals(0, fibHeap.size());
        assertNull(fibHeap.findMin());
    }

    @Test
    void TestDeleteMinEmpty() {
        FibonacciHeap fibHeap = new FibonacciHeap();
        fibHeap.deleteMin();
        assertEquals(0, fibHeap.size());
        assertNull(fibHeap.findMin());
    }
}