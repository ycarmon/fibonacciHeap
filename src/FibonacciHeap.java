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
    // A counter of the number the heap performs a heap action
    // Used to return totatlLinks()
    private static int totalLinks = 0;
    private static int totalCuts  = 0;
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
            this.roots.remove(node);

            if (0 == this.roots.getSize()){
                this.minNode = null;
            }else {
                this.minNode = roots.head;  // TODO: this doesn't seem right
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

                    if (curr.key > inBucket.key)
                        curr = link(inBucket, curr);
                    else curr = link(curr, inBucket);
                    // After a link - the previous bucket is empty
                    rankArray[currNodeRank] = null;

                    currNodeRank++;
                    if (currNodeRank > this.topRank)
                        this.topRank = currNodeRank;
                }

                rankArray[currNodeRank] = curr;
            }
        }
        populateRoots(rankArray);

    }

    /**
     * This is a spearate method for code clarity reasons
     * links a two trees of the same rank.
     * Complexity: derivesd from link function and is O(1)
     * @param curr a tree that is the by-product of the consolidation process
     * @param bucketTreeRoot - the tree in curr's rank bucket.
     * @return the new linked tree of rank: (curr.rank + 1)
     */
    private HeapNode linkWithRankBucketTree(HeapNode curr, HeapNode bucketTreeRoot){
        if (curr.key > bucketTreeRoot.key)
            return link(bucketTreeRoot, curr);

        return link(curr, bucketTreeRoot);
    }

    /**
     * After the consolidation process is finished, we add the new roots in the rank Array
     * to the roots linked list and we find the minimal node of the heap
     * (the smallest key among the roots)
     * Complexity O(n) where n is the number of roots in the tree
     */
    private void populateRoots(HeapNode [] rankArray) {
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
     * @param newRoot The root of the tree that will be the root of the new tree
     * @param child The root of the tree that will be the child in the new tree
     * @return The root of the resulting tree.
     */

    private HeapNode link(HeapNode newRoot, HeapNode child) {
        //Remove child from roots,
        this.roots.remove(child);
        // Set new parent-child relationship
        newRoot.getChildren().insert(child);
        child.parent = newRoot;
        newRoot.rank++;

        // Update counter link actions performed:
        FibonacciHeap.totalLinks++;

        return newRoot;
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
            double bound =  constant * Math.log((double) size) / Math.log(2);
            int retval = (int) Math.ceil(bound);
            return retval;
        }

        return 0;
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
    * Return a counters array, where the value of the i-th entry
     * is the number of trees of order i in the heap.
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
        if (null != x) {
            int delta = (x.getKey() - this.findMin().getKey())+1;
            decreaseKey(x, delta);
            deleteMin();
        }
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    */
    // OFEK: add tests
    // OFEK: add complexity documentation
    public void decreaseKey(HeapNode x, int delta)
    {
    	int newKey = x.getKey() - delta;
        if (x.parent != null && newKey < x.parent.getKey())
        {
            cascadingCut(x, x.parent);
        }
        x.setKey(newKey);
        if (this.findMin().getKey() > x.getKey())
            this.minNode = x;
    }


    // cascading cut function
    public void cascadingCut(HeapNode x, HeapNode y)
    {
        // the actual cutting
        x.parent = null;
        if (x.mark) {
            x.mark = false;
            this.nodes_marked--;
        }
        y.getChildren().remove(x);
        y.rank--;
        this.roots.insert(x);

        FibonacciHeap.totalCuts++;

        // cascading the cut upwards
        if (y.parent != null) {
            if (y.mark)
                cascadingCut(y, y.parent);
            else {
                y.mark = true;
                this.nodes_marked++;
            }
        }
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
    	return this.roots.getSize() + 2*this.nodes_marked;
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
    	return FibonacciHeap.totalLinks;
    }

   /**
    * public static int totalCuts()
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods).
    */
    public static int totalCuts()
    {
    	return FibonacciHeap.totalCuts; // should be replaced by student code
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * This static function returns the k minimal elements in a binomial tree H.
     * The function should run in O(k*deg(H)).
     * You are not allowed to change H.*
     * The function starts by adding H's roots to a new Fibonacci heap. O(1) * number_of_roots
     *Then iteratively k -times,
     *     get the minimal element's key, O(1)
     *     add it to the return array.
     *     perform delete-min on the helper heap.
     *     add all of the deleted node's children to the helper heap.
     * Complexity: O(k * deg(H)):
     *      Firstly the function adds all of H's roots to the helper heap
     *      O(1) * number_of_roots. However since H is a binomial tree it
     *      has only a single root thus number_of_roots is 1.
     *      Thus the loop runs at O(1).
     *
     *      The second loop runs k times and in each iteration performs the
     *      following sequence of actions:
     *          a findMin call - O(1)
     *          a deleteMin call - O(log n) - where is the number of nodes in the helper heap,
     *              in the worst case, every node from H is in the helper heap thus this runs
     *              at O(log 2^{deg H}) = O(deg H).
     *          a secondary loop that adds the minimal node's roots to the helper heap
     *              O(1) * number_of_roots. Since there are at most deg(h) roots to this runs in
     *              O(deg H)
     *       The total complexity of this loop is  k * ( O(1) + O(deg H) + O(deg H)) = O(k deg H)
     *
     *       So the total runtime complexity of this function is  O(1)+O(k deg H) = O(k deg H)
     */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        int[] arr = new int[k];
        FibonacciHeap fibHeap = new FibonacciHeap();
        HeapNode minNode = H.findMin();
        for (HeapNode root : H.roots) {
            HeapNode currNode = fibHeap.insert(root.getKey());
            currNode.nodePointer = root; // Point to the matching node in H
        }

        for (int i = 0; i < k; i++) {
            arr[i] = fibHeap.findMin().getKey();
            NodeLL min_children = fibHeap.findMin().nodePointer.children;
            fibHeap.deleteMin();
            for (HeapNode child : min_children)
            {
                HeapNode currNode = fibHeap.insert(child.getKey());
                currNode.nodePointer = child; // Point to the matching node in H
            }

        }

        return arr; // should be replaced by student code
    }

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
       public boolean mark;


       // instead of a pointer to a random child -
       // a pointer to a linked list of the children.
       private NodeLL children;

       // This nodes right sibling in the linked list
       private HeapNode next;
       // This nodes left sibling in the linked list
       private HeapNode prev;

       // this node's parent
       private  HeapNode parent;

       // a pointer field to another node,
       // this would inter-heap referencing
       // OFEK: what is this?
       public HeapNode nodePointer;


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

        public void setKey(int newKey) {
            this.key = newKey;
        }

   }

    /**
     * A class used to represent the nodes in a vertex as a linked list,
     * This is neede instead of Java's implementation as we need to manage references ourselves.
     */
    public class NodeLL implements Iterable<HeapNode> {
        //first node in list
        private HeapNode head;
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
            HeapNode lastHead = this.head;
            this.head = node;

            if (lastHead == null)
            {
                node.next = node;
                node.prev = node;
            }
            else
            {
                node.next = lastHead;
                node.prev = lastHead.prev;

                lastHead.prev.next = node;
                lastHead.prev = node;
            }

            this.size++;
        }

        /**
         * Joins two node lists by concatenating one list at the back of on other
         * Maintains the list as circular.
         * As we say in class Complexity is O(1), as we only manipulate
         * a constant number of pointers
         * @param other
         */
        private void join (NodeLL other) {

            if (this.getSize() == 0)
            {
                this.head = other.head;
                this.size = other.size;
            }
            else if (other.getSize() != 0) {
                HeapNode thisTail = this.head.prev;
                HeapNode otherTail = other.head.prev;

                thisTail.next = other.head;
                other.head.prev = thisTail;

                this.head.prev = otherTail;
                otherTail.next = this.head;

                this.size = this.getSize() + other.getSize();
            }
        }

        /**
         * Removes a specified heap node from the list
         * @preconidtion: The node is in the linked list
         * @param removed
         */
        private void remove(HeapNode removed) {
            if (this.getSize() != 1){
                // If the list isn't empty we want to set the head to be the
                // old head's next node. Otherwise we want it be null
                if (removed == this.head) this.head = removed.next;

                HeapNode prev = removed.prev;
                HeapNode next = removed.next;
                prev.next = next;
                next.prev = prev;
            }
            else {
                if (removed == this.head) this.head = null;

                removed.next = null;
                removed.prev = null;
            }

            this.size--;
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
            // Current HeapNode object to be returned in the next call of next()

            HeapNode curr;
            boolean notInitial;

            public NodeLLIterator() {
                this.notInitial = false;
                this.curr = head;
            }

            @Override
            public boolean hasNext() {
                if (curr == null) return false;

                return !(curr == head && notInitial);
            }

            @Override
            public HeapNode next() {
                HeapNode ret = curr;
                curr = curr.next;
                notInitial = true;
                return ret;
            }
        }
    }
}

