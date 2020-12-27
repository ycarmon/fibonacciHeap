import java.util.LinkedList;
import java.util.Vector;
/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
    // This is used to remove min node from the vector without the need to find it.
    private int minIndex = -1;
    private int size;
    private int nodes_marked;


    private LinkedList<HeapNode> roots;

    public FibonacciHeap()
    {
        this.roots = new LinkedList<HeapNode>();
    }

   /**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *
    * Complexity: As seen in class this runs in O(1) and only requires field access
    */
    public boolean isEmpty()
    {
    	return (this.findMin() == null && 0 == this.size);
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * The new new node is inserted as a binomial tree of rank 0, to the root's list.
    * Complexity: since we used the lazy insertion method, as seen in class insert runs at O(1)
    * Returns the new node created. 
    */
    public HeapNode insert(int key)
    {
        HeapNode node = new HeapNode(key);
        this.roots.add(node);
        if (isEmpty() || node.key < this.findMin().getKey()){
            this.minIndex = this.getRootsLinkedList().size();
        }
        this.size++;
    	return node;
    }

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    * Complexity: O(log n) The runtime complexity is determined by the complexity of
    * the consolidation process which takes place in a helper function.
    * The consolidation runs at an amortized time of O(log n) hence this runs at O(log n)
    */
    public void deleteMin()
    {
        HeapNode node = this.findMin();
        if (null != node){
            this.roots.addAll(node.children);
        }

        // remove min from roots
        this.roots.remove(this.getMinIndex());

        if (this.roots.size() == 0) {
            this.minIndex = -1;
        }

     	return; // should be replaced by student code
     	
    }

   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    * simply return the private field min
    * Complexity O(1)
    */
    public HeapNode findMin()
    {
        if (this.getMinIndex() == -1)
            return null;

        return this.roots.get(this.minIndex);
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (FibonacciHeap heap2)
    {
        // If the second heap is empty, we don't need to change our own heap
        if (heap2.isEmpty())
            return;

        // Add all roots of heap2 to this roots vector
        this.roots.addAll(heap2.getRootsLinkedList());

        //if current heap is empty
        if (this.isEmpty() || heap2.findMin().getKey() < this.findMin().getKey()){
            this.minIndex = heap2.getMinIndex();
        }
        else {
            this.consolidate();
        }

        this.size += heap2.size();
    }


   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return this.size; // should be replaced by student code
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    *  It was clarified in the forum that the tree's order is the root's rank.
    */
    public int[] countersRep()
    {

    int[] rootsRankArr = new int[roots.size()];
    for (HeapNode root : this.roots){
        if (root != null)
            rootsRankArr[root.rank]++;
    }

    return rootsRankArr; //	 to be replaced by student code
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) 
    {    
    	return; // should be replaced by student code
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    	return; // should be replaced by student code
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
    	return 0; // should be replaced by student code
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks()
    {    
    	return 0; // should be replaced by student code
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return 0; // should be replaced by student code
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k minimal elements in a binomial tree H.
    * The function should run in O(k*deg(H)). 
    * You are not allowed to change H.
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {    
        int[] arr = new int[42];
        return arr; // should be replaced by student code
    }

    // A getter to the heap's min element

    // A getter to the heap's number of marked nodes
    public int getNodesMarked() {
        return this.nodes_marked;
    }

    public int getMinIndex()
    {
        return this.minIndex;
    }
    public LinkedList<HeapNode> getRootsLinkedList()
    {
        return this.roots;
    }
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
    public class HeapNode{

       public int key;
       public int rank;
       private LinkedList<HeapNode> children;
       public HeapNode(int key) {
           this.children = new LinkedList<HeapNode>();
           this.key = key;
       }

       public int getKey() {
           return this.key;
       }

       public LinkedList<HeapNode> getChildren() {
           return children;
       }
   }
}
