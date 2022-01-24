package weightedgpa.infinibiome.internal.generators.utils;

import java.util.*;
import java.util.function.Predicate;

public final class SortedRandomizedList<T>{
    private final List<ItemGroup> itemGroups = new ArrayList<>();
    private final Comparator<T> comparator;

    public SortedRandomizedList(Iterable<T> items, Comparator<T> comparator) {
        this.comparator = comparator;

        initGroups(items);
    }

    private void initGroups(Iterable<T> items){
        for (T item: items){
            addItemToGroups(item);
        }

        itemGroups.sort(ItemGroup::compareTo);
    }

    private void addItemToGroups(T item){
        for (ItemGroup group: itemGroups){
            if (group.addIfPartOfGroup(item)){
                return;
            }
        }
        itemGroups.add(new ItemGroup(item));
    }

    public void forEachItem(Predicate<T> itemConsumer){
        for (ItemGroup group: itemGroups){
            group.perItem(itemConsumer);
        }
    }


    private class ItemGroup implements Comparable<ItemGroup>{
        private final List<T> items = new ArrayList<>();

        private ItemGroup(T item) {
            this.items.add(item);
        }

        boolean addIfPartOfGroup(T item){
            if (comparator.compare(item, items.get(0)) == 0){
                items.add(item);
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(ItemGroup other) {
            return comparator.compare(this.items.get(0), other.items.get(0));
        }

        void perItem(Predicate<T> itemConsumer){
            List<T> randomizedGroup = new ArrayList<>(items);

            Collections.shuffle(randomizedGroup);

            for (T item: randomizedGroup){
                boolean continueFlag = itemConsumer.test(item);

                if (!continueFlag){
                    break;
                }
            }
        }
    }
}

