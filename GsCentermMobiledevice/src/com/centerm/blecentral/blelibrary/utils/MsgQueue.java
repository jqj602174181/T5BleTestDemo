package com.centerm.blecentral.blelibrary.utils;

/**
 * Created by jqj on 2016/5/24.
 * 闁炬崘銆冪�圭偟骞囬惃鍕Е閸掞拷
 */
public final class MsgQueue<T> {
    private Node first;
    private Node last;
    private int length;

    public boolean isEmpty() {
        return length == 0;
    }

    public int size() {
        return length;
    }

    public void enQueue(T item) {
        Node oldlast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        if (isEmpty()) {
            first = last;
        } else {
            oldlast.next = last;
        }
        length++;

    }

    public T deQueue() {
        if (isEmpty()) {
            last = null;
            return null;
        }
        T values = first.item;
        first = first.next;
        length--;
        return values;
    }

    private class Node {
        T item;
        Node next;
    }
}
