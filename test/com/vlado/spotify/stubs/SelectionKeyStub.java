package com.vlado.spotify.stubs;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class SelectionKeyStub extends SelectionKey {

    int interestedOps;

    @Override
    public SelectableChannel channel() {
        return null;
    }

    @Override
    public Selector selector() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public int interestOps() {
        return interestedOps;
    }

    @Override
    public SelectionKey interestOps(int ops) {
        interestedOps = ops;
        return this;
    }

    @Override
    public int readyOps() {
        return 0;
    }

    @Override
    public int interestOpsOr(int ops) {
        return super.interestOpsOr(ops);
    }

    @Override
    public int interestOpsAnd(int ops) {
        return super.interestOpsAnd(ops);
    }

}
