package com.zk.base;

public class Money {

    private long amount;
    private long count;

    public Money(long amount, long count) {
        this.amount = amount;
        this.count = count;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
