public class Cons<E> implements ImList<E> {
    private final E e;
    private final ImList<E> rest;

    public Cons(E e, ImList<E> rest) {
        this.e = e;
        this.rest = rest;
    }

    public ImList<E> cons(E e) {
        return new Cons<>(e, this);
    }

    public E first() {
        return e;
    }

    public ImList<E> rest() {
        return rest;
    }

    public int size() {
        return 1 + rest.size();
    }

    @Override
    public String toString() {
        return e.toString() + rest.toString();
    }
}