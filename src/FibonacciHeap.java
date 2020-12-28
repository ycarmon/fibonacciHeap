import javax.xml.soap.Node;
import java.util.Iterator;
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
    private HeapNode minNode;
    private int size;
    private int nodes_marked;

    // the highest rank of any tree in the heap, used for
    // array allocation in countersRep
    private int topRank;
    private NodeLL roots;

    public FibonacciHeap()
    {
        this.roots = new NodeLL();
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
        HeapNode node = this.roots.insert(key);
        if (isEmpty() || node.key < this.findMin().getKey()){
            this.minNode = node;
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
            NodeLL children = node.getChildren();
            if (children.getSize() != 0) {
                for (HeapNode child : node.getChildren()) {
                    child.parent = null;
                }
                roots.join(children);
            }
            // remove min from roots
            this.roots.remove(this.findMin());

            if (0 == this.roots.getSize())
                this.minNode = null;
            else {
                this.minNode = roots.head;
                consolidate();
            }
            this.size--;
        }
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
        return this.minNode;
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
        this.roots.join(heap2.roots);

        //if current heap is empty
        if (this.isEmpty() || heap2.findMin().getKey() < this.findMin().getKey()){
            this.minNode = heap2.findMin();
        }

        // adjust topRank
        if (this.topRank < heap2.getTopRank())
            this.topRank = heap2.topRank;

        this.size += heap2.size();
    }

    /**
     * Reduce the number of trees in the heap to one per rank, and find the
     * minimal key in the heap.
     * Tje consolidation process uses an helper array to sort the trees in
     * the heap by the ranks.
     * The size of the roots array might be the entire size of three O(log n)
     *
     * The size of the rank array is calculated
     */
    private void consolidate(){
        HeapNode[] rankArray = new HeapNode[getRankBound(this.size())];
        //HeapNode[] rankArray = new HeapNode[];
        int rootsArrLen = roots.getSize();
        HeapNode node = roots.head;

        for (int i = 0; i < rootsArrLen; i++) {
            HeapNode curr = node;
            node = node.next;

            if (curr != null)
            {
                int currNodeRank = curr.rank;
                while (rankArray[currNodeRank] != null) {
                    HeapNode inBucket = rankArray[currNodeRank];
                    if (curr.key > inBucket.key) {
                        curr = link(inBucket, node);
                    } else {
                        curr = link(node, inBucket);
                    }

                    // After the consolidation, this rank's bucket is empty
                    rankArray[currNodeRank] = null;
                    currNodeRank++;
                    if (currNodeRank > this.topRank)
                        this.topRank = currNodeRank;
                }

                rankArray[currNodeRank] = node;
            }
        }

        this.minNode = null;
        this.roots = new NodeLL();

        // Repopulate the roots linkedList
        for (HeapNode root : rankArray) {
            if (null != root) {
                roots.insert(root);
                if (this.findMin() == null || root.key < this.findMin().getKey()) {
                    this.minNode = root;
                }
            }
        }
    }

    /**
     * Links two trees of the same rank into a new tree with rank that is greater by 1
     *
     * THis method runs in O(1): Only requires a change to a constant number of nodes.
     * @param parent The root of the tree that will be the root of the new tree
     * @param child Tje root of the tree that will be the child in the new tree
     * @return The root of the resulting tree.
     */

    private HeapNode link(HeapNode parent, HeapNode child) {
        this.roots.remove(child);
        parent.getChildren().insert(child);
        child.parent = parent;
        parent.rank++;

        return parent;
    }

    /**
     * Calculate the upper bound of ranks in the heap as ssen in class.
     * were the bound is 1.4404 log_2 n
      * @param size
     * @return The upper bound to the heaps ranks
     */
    private int getRankBound(int size){
        if (this.size() != 0) {
            double constant = 1.4404;
            double bound =  log2OfSize(size) * constant;
            int retval = (int) Math.ceil(bound);
            return retval;
        }

        return 0;
    }


    //get the base 2 log of an integer used for bound calculation
    private double log2OfSize(int size) {
        return (Math.log((double) size) / Math.log(2));
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
        // Heap is a list, we just have trees of rank 0,
        // but all trees are in this index
        if (this.getTopRank() == 0) {
            int [] rootRankArr = {roots.getSize()};
            return rootRankArr;
        }

        int[] rootsRankArr = new int[this.getTopRank() + 1];
        for (HeapNode root : this.roots){
            if (root != null)
                rootsRankArr[root.rank]++;
        }

        return rootsRankArr;
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

    public int getTopRank()
    {
        return this.topRank;
    }

    public NodeLL getRootsLinkedList()
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

       // this node's parent
       private  HeapNode parent;
       // This nodes right sibling in the linked list
       private HeapNode next;

       // This nodes left sibling in the linked list
       private HeapNode prev;
       private NodeLL children;

       public HeapNode(int key) {
           this.children = new NodeLL();
           this.key = key;
       }

       public int getKey() {
           return this.key;
       }

       public NodeLL getChildren() {
           return children;
       }
   }

    /**
     * A class used to represent the nodes in a vertex as a linked list,
     * This is neede instead of Java's implementation as we need to manage references ourselves.
     */
   public class NodeLL implements Iterable<HeapNode> {
       //first node in list
          private HeapNode head;
        //last node in list
        private HeapNode tail;
        private int size;

        /**
         * Insert a new now to the end of the list, this will be the new tail
         * Complexity O(1) - only requires local changes of pointers
         * @param key the key of the new inserted node
         * @return a pointer to the new inserted node
         */
        private HeapNode insert (int key) {
            HeapNode node = new HeapNode(key);
            insert(node);

            return node;
        }

        /**
         * Insert an existing node at the beginning of the list, this will be the lists new head
         * Complexity O(1) - only requires local changes of pointers
         * @param node The new heap node added to the list
         */
        private void insert(HeapNode node) {
            if (null == this.head) {
                this.head = node;
                this.tail = node;
            } else {
                head.prev = node;
                node.next = head;
                head = node;
            }

            tail.next = head;
            head.prev = tail;

            this.size= 1 + getSize();
        }

        /**
         * Joins two node lists by concatenating one list at the back of on other
         * Maintains the list as circular.
         * As we say in class Complexity is O(1), as we only manipulate
         * a constant number of pointers
         * @param other
         */
        private void join (NodeLL other) {
            if (other != null && other.getSize() != 0) {
                // set other.tail as the new tail
                this.tail.next = other.head;
                this.tail = other.tail;
                // connect the tail to the head
                other.head.prev = this.tail;
                head.prev = tail;
                tail.next = head;

                this.size = this.getSize() + other.getSize();
            }
        }

        /**
         * Removes a specified heap node from the list
         * @preconidtion: The node is in the linked list
         * @param removed
         */
        private void remove(HeapNode removed) {
            if (removed == this.head) {
                if (this.getSize() != 1)
                    this.head = removed.next;
                else
                    this.head = null;
            }

            if (removed == this.tail) {
                if (this.getSize() != 1)
                    this.tail = removed.next;
                else
                    this.tail = null;
            }
            if (getSize() != 1) {
                HeapNode prev = removed.prev;
                HeapNode next = removed.next;

                prev.next = next;
                prev.prev = prev;
            }
            removed.next = null;
            removed.prev = null;
            this.size = this.getSize() - 1;
        }

        /**
         * Return an iterator for the linked list
          * @return A NodeLL iterator
         */
        @Override
        public Iterator iterator() {
            return new NodeLLIterator();
        }

        public int getSize() {
            return  this.size;
        }

       private class NodeLLIterator implements Iterator<HeapNode> {
           /**
            * True if all the nodes in the list has been visited (returned by next()
            */
           boolean done = false;
           // Current HeapNode object to be returned in the next call of next()
           HeapNode curr;

           @Override
           public boolean hasNext() {
               if (null == head)
                   done = true;

               return !done;
           }

           @Override
           public HeapNode next() {
                if (this.curr == null)
                    curr = head;
                else
                    curr = curr.next;

                if (curr == tail)
                    done = true;

                return curr;
           }
       }
    }
}

