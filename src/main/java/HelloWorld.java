/**
 * Created by Liudmila Kornilova
 * on 13.05.17.
 */
public class HelloWorld {
    public static void fun2 () {

    }
    public static void fun3 () {

    }
    public static void fun1 () {
        fun2();
        fun3();
    }

    public static void main(String[] args) {
        System.out.println("hello, world!");
        fun1();
        fun2();
    }
}
