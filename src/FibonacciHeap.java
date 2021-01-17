import java.util.Iterator;

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
    private HeapNode minNode;

    private int size;
    private int nodes_marked;

    private static int totalLinks = 0;
    private static int totalCuts  = 0;

    // TODO: change implementation to not use that.
    // the highest rank of a tree in the heap, used for
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
    * Runtime Complexity: Runs in O(1) and only requires field access.
    */
    public boolean isEmpty()
    {
    	return (this.size == 0);
    }


   /**
    * public HeapNode insert(int key)
    *
    * Creates a HeapNode with the input key, and the new node is inserted as a
    * binomial tree of rank 0, to the roots' doubly-linked-list.
    *
    * Complexity: Using the lazy insertion routine from class, insert runs at 
    * O(1).
    *
    * The type of this.roots is NodeLL: we implemented a doubly-linked-list of
    * HeapNodes, and insertion to it runs in O(1).
    * 
    * Returns a pointer to the new node.
    */
    public HeapNode insert(int key)
    {
        HeapNode node = this.roots.insert(key);

        // update minNode
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
    * Complexity: Amortized - O(log n) ; WC - O(n)
    * The runtime complexity is determined two phases:
    * 1) Changing the parent pointers of the deleted-node's children. So the
    *    amortized runtime of this part is O(log n).
    * 2) Consolidation - a helper function. The implementation is the same as
    *    the one wev'e seen in class: amortized runtime - O(log n) and WC 
    *    runtime is O(n).
    */
    public void deleteMin()
    {
        HeapNode node = this.findMin();

        if (node != null) {

            // add children as roots
            NodeLL children = node.getChildren();
            for (HeapNode child : children) {
                child.parent = null;
            }

            // removing the node, and melding the children
            roots.join(children);
            this.roots.remove(node);

            // consolidating
            this.minNode = null;
            consolidate();

            this.size--;
        }
    }


   /**
    * Reduce the number of trees to amaximum of one tree per rank, then
    * finding the minimal key.
    *
    * The consolidation process uses an helper array as the 'buckets' - that
    * hold trees with different ranks.
    *
    * The implementation is 
    */
    private void consolidate(){
        int maxRank = getRankBound(this.size()) // 1.4404 * log2(size)
        HeapNode[] rankArray = new HeapNode[maxRank];

        HeapNode node = roots.head;

        for (int i = 0; i < roots.getSize(); i++) {
            HeapNode curr = node;
            node = node.next;

            if (curr != null)
            {

                int currRank = curr.rank;
                while (rankArray[currRank] != null) {
                    HeapNode inBucket = rankArray[currRank];

                    if (curr.key > inBucket.key)
                        curr = link(inBucket, curr);
                    else
                        curr = link(curr, inBucket);

                    // After a link - the previous bucket is empty
                    rankArray[currRank] = null;

                    currRank++;
                    if (currRank > this.topRank)
                        this.topRank = currRank;
                }

                rankArray[currRank] = curr;
            }
        }

        populateRootsFindMin(rankArray);

    }

   /**
    * Calculate the upper bound of ranks in the heap: 1.4404*log2(n) 
    * @param size
    * @return The upper bound to the heaps ranks
    */
    private int getRankBound(int size){
        if (size == 0) return 0;

        double constant = 1.4404;
        double bound =  constant * Math.log((double) size) / Math.log(2);
        return (int) Math.ceil(bound);
    }

    /**
     * After the consolidation process is finished, we add the new roots in the
     * rank Array to the roots linked list and we find the minimal node of the 
     * heap (the smallest key among the roots)
     *
     * Complexity O(n) where n is the number of roots in the tree
     */
    private void populateRootsFindMin(HeapNode [] rankArray) {
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

        // update minNode
        if (this.isEmpty() || heap2.findMin().getKey() < this.findMin().getKey()){
            this.minNode = heap2.findMin();
        }

        // udpate topRank
        if (this.topRank < heap2.getTopRank())
            this.topRank = heap2.topRank;

        this.size += heap2.size();
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
    * public int size()
    *
    * Return the number of elements in the heap.
    *
    * O(1) - an access to a class field.
    *
    */
    public int size()
    {
    	return this.size; 
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

        // int[] rootsRankArr = new int[this.getTopRank() + 1];
        int rankBound = getRankBound(this.getSize()) + 1;
        int[] rootsRankArr = new int[rankBound];
        for (HeapNode root : this.roots){
            if (root != null)
                rootsRankArr[root.rank]++;
        }

        // get rid of zeroes
        int maxRank = rankBound;
        while (maxRank > 0) {
            if (rootRankArr[maxRank-1] > 0) break;
            maxRank--;
        }
        int[] rootsRankShortArr = new int[maxRank]
        for (int i = maxRank; i > 0; i--) {
            rootsRankShortArr[i-1] = rootsRankArr[i-1]
        }

        return rootsRankShortArr[];
    }


   /**
    * public void delete(HeapNode x)
    * Deletes the node x from the heap by decreasing it's key so it
    * is the new minimal node and then preforming deleteMin
    *
    * Runtime Complexity: O(log n)
    * delete's complexity derives from the complexity of the two functions it calls
    * decreaseKey and deleteMin, as we saw in class, both functions run at O(log n) and
    * since they are called one after another the total complexity is
    * O(log n) + O(log n) = O(log n)
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
    * The function decreases the key of the node x by delta. The structure of
    * the heap should be updated to reflect this change (for example, the
    * cascading cuts procedure should be applied if needed).
    *
    * Runtime Complexity: O(log n) - the implementation is according to the one
    * we learned in class.
    * 
    *
    *
    * delete's complexity derives from the complexity of the two functions it calls
    * decreaseKey and deleteMin, as we saw in class, both functions run at O(log n) and
    */
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
    * 
    * Runtime complexity - O(1).
    */
    public int potential()
    {
    	return this.roots.getSize() + 2*this.nodes_marked;
    }

   /**
    * public static int totalLinks()
    *
    * This static function returns the total number of link operations made
    * during the run-time of the program.
    * 
    * Runtime complexity - O(1).
    */
    public static int totalLinks()
    {
    	return FibonacciHeap.totalLinks;
    }

   /**
    * public static int totalCuts()
    *
    * This static function returns the total number of cut operations that
    * were made during the run-time of the program.
    * 
    * Runtime complexity - O(1).
    */
    public static int totalCuts()
    {
    	return FibonacciHeap.totalCuts; 
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * This static method returns the k minimal elements in a BINOMIAL TREE H.
     * 
     * For a tree with n nodes the function's runtime complexity is 
     * O(k*log(n)).
     *
     * The function must not modify H.
     * If we could modify H we would use deleteMin k times - thus achieving the
     * desired runtime complexity:
     *      - For each deleteMin operation the minimal node has O(log(n))
     *        children. And the number of trees in the heap would be
     *        bounded by O(log(n)).
     *      - Thus - before each consolidate operation the number of trees is
     *        less than 2log(n) trees, and after the consolidate operation the
     *        number of trees is less than log(n). So the number of links, and
     *        the deleteMin runtime complexity would be O(log(n)).
     *
     * In order to not change H - we use a helper heap, in which we would delete
     * nodes, and every node would maintain a pointer to its corresponding node
     * in the original heap.
     *
     * Following is a description of the algorithm:
     * 1. Create a Helper FibonacciHeap with a single nodes that points to the 
     *    root of H. Runs in O(1).
     * 2. Iteratively: (k iterations)
     *  2.1. DeleteMin on the helper heap - O(log(n)).
     *  2.2. For that deleted node,iIterate over the children of corresponding
     *       in the Original heap, and add them as roots to the helper heap.
     *       This part also runs in O(log(n)).
     * 
     * Thus, the runtime complexity of the algorithm is:
     *      O(k*(log(n)+log(n))) = O(k*log(n))
     *
     */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        int[] arr = new int[k];
        // creating a helper FibonacciHeap
        FibonacciHeap fibHeap = new FibonacciHeap();


        // this loop has a single iteration - because H 
        // is a binomial-tree and has a single root.
        for (HeapNode root : H.roots) {
            HeapNode currNode = fibHeap.insert(root.getKey());
            currNode.nodePointer = root;
        }

        // using the node pointers - extracting the k smallest
        // nodes using deleteMin on the helper FibonacciHeap
        for (int i = 0; i < k; i++) {
            arr[i] = fibHeap.findMin().getKey();
            // get the minimal node children using the nodepointer
            NodeLL min_children = fibHeap.findMin().nodePointer.children;
            // delete the minimal node from the helper heap
            fibHeap.deleteMin();

            for (HeapNode child : min_children)
            {
                // make the children point to their corresponding nodes in
                // the original heap
                HeapNode currNode = fibHeap.insert(child.getKey());
                currNode.nodePointer = child;
            }

        }

        return arr; 
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
         * Insert an existing node at the beginning of the list, this will be 
         * the lists new head.
         *
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

