public class Empty<E> implements ImList<E> {
    public Empty() {
    }

    public ImList<E> cons(E e) {
        return new Cons<>(e, this);
    }

    public E first() {
        throw new UnsupportedOperationException();
    }

    public ImList<E> rest() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return 0;
    }

    public String toString() {
        return "";
    }
}