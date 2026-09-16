package market;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class EventLog<T> {
    private final int capacity;
    private final Deque<T> items = new ArrayDeque<>();

    public EventLog(int capacity) {
        this.capacity = capacity;
    }

    public void add(T item) {
        items.addLast(item);
        while (items.size() > capacity) {
            items.removeFirst();
        }
    }

    public List<T> recent() {
        List<T> list = new ArrayList<>(items);
        Collections.reverse(list);
        return list;
    }

    public int size() { return items.size(); }
}