package com.zk.base;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {

        System.out.println("ZXH" + System.currentTimeMillis() + 100000000L);
        /*List<Money> cardsList = new ArrayList<>();
        cardsList.add(new Money(48l,2l));
        cardsList.add(new Money(18l,2l));
        cardsList.add(new Money(28l,2l));
        cardsList.add(new Money(38l,2l));
        cardsList.add(new Money(50l,1l));
        cardsList.add(new Money(30l,1l));
        cardsList.add(new Money(10l,2l));
        cardsList.add(new Money(5l,8l));
        cardsList.add(new Money(3l,4l));
        cardsList.add(new Money(1l,10l));
        Collections.sort(cardsList, new Comparator<Money>() {
            @Override
            public int compare(Money obj1, Money obj2) {
                return obj1.getAmount() >= obj2.getAmount() ? -1 : 1;
            }
        });
        long amount = (long)(Math.random()*600);
        System.out.println(amount);
        List<Money> resultList = cal(cardsList, amount, 0);
        for (Money po : resultList){
            System.out.println(po.getAmount());
        }*/
    }

    private static List<Money> cal(List<Money> cardsList, long amount, long total) {
        List<Money> resultList = new ArrayList<>();
        Money po;
        for (int i = 0; i < cardsList.size(); i++) {
            po = cardsList.get(i);
            if (po.getCount() <= 0 || total + po.getAmount() > amount) {
                continue;
            }
            resultList.add(po);
            po.setCount(po.getCount() - 1);
            if (total + po.getAmount() == amount) {
                return resultList;
            }
            total += po.getAmount();
            List<Money> list = cal(cardsList, amount, total);
            if (list != null) {
                resultList.addAll(list);
                return resultList;
            }
        }
        return null;
    }

}
