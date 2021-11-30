package VMmachine;

public interface Stack {

    void push(Object x);

    Object pop();

    Object top();

    boolean isEmpty();
}
