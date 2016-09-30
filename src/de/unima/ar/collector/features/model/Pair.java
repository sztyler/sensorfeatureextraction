package de.unima.ar.collector.features.model;

/**
 * Generic pair class that is used to store the gravity and acceleration vector separately.
 *
 * @author Timo Sztyler
 * @version 30.09.2016
 */
public class Pair<L, R> {
    private L left;
    private R right;


    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }


    public void setLeft(L left) {
        this.left = left;
    }


    public void setRight(R right) {
        this.right = right;
    }


    public L getLeft() {
        return left;
    }


    public R getRight() {
        return right;
    }
}