import java.util.Scanner;

/**
 * ReplStack Driver
 */
public class Main {

    public static void testStack() {
        try {
            ReplStack<Integer> stack1 = new ReplStack<>("temp");
            stack1.push(1);

            ReplStack<Integer> stack2 = new ReplStack<>("temp");
            stack2.push(2);

            stack1.push(3);
            System.out.println("stack2.pop() -> " + stack2.pop()); // 3
            System.out.println("stack1.pop() -> " + stack1.pop()); // 2
            System.out.println("stack2.pop() -> " + stack2.pop()); // 1

            try {
                System.out.println("stack1.pop() -> " + stack1.pop()); // Error: EmptyStackException
            } catch (Exception e) {
                System.out.println("stack is empty");
            }

            stack1.close();
            stack2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testSet() {
        try {
            ReplSet<Integer> set1 = new ReplSet<>("temp");
            set1.add(1);

            ReplSet<Integer> set2 = new ReplSet<>("temp");
            set2.add(2);

            System.out.println("set1 contains 2: " + set1.contains(2)); // True
            System.out.println("set1 contains 3: " + set1.contains(3)); // False

            set1.remove(2);
            System.out.println("set2 contains 2: " + set2.contains(2)); // False

            set1.close();
            set2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testStack();
        testSet();
    }

}
