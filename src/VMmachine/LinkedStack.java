package VMmachine;

public class LinkedStack implements Stack {
    public  LinkedStack() {
        topOfStack = null;
    }

    @Override
    public boolean isEmpty() {
        return topOfStack == null;
    }

    @Override
    public void push(Object x) {
        topOfStack = new ListNode(x,topOfStack);
    }

    @Override
    public Object top() {
        if (isEmpty()){
            throw new UnderflowException("Exception occurred while POP operation happened on Stack");
        }
        return  topOfStack.element;
    }

    @Override
    public Object pop() {
        if (isEmpty()){
            throw new UnderflowException("Exception occurred while POP operation happened on Stack");
        }
        Object topItem = topOfStack.element;
        topOfStack = topOfStack.next;
        return topItem;
    }
    private ListNode topOfStack;
}
