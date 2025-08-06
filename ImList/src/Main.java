public class Main {
    public static void main(String[] args) {
        ImList<Integer> lst = ImList.empty();
        ImList<Integer> x = lst.cons(2).cons(1).cons(0);
        System.out.println(x);
        System.out.println(x.size());
    }
}
