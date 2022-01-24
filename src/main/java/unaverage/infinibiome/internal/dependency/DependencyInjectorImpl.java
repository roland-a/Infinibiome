package weightedgpa.infinibiome.internal.dependency;

import weightedgpa.infinibiome.api.dependency.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public final class DependencyInjectorImpl implements DependencyInjector.Pre {
    final List<ItemWrapper<?>> items = new ArrayList<>();
    final List<Consumer<DependencyInjector>> staticItems = new ArrayList<>();
    final List<Predicate<MultiDep>> blackList = new ArrayList<>();

    public DependencyInjectorImpl(List<DependencyModule> modules){
        modules.forEach(m -> m.addToPre(this));
    }

    @Override
    public <T> DependencyInjectorImpl addItem(Class<T> clazz, Function<DependencyInjector, T> toItem){
        /*
        Validate.isTrue(
            MultiDep.class.isAssignableFrom(clazz) || SingleDep.class.isAssignableFrom(clazz)
        );
         */

        ItemWrapper<T> item = new ItemWrapper<>(clazz, toItem);

        items.add(item);

        return this;
    }

    @Override
    public <T, E> DependencyInjectorImpl addItems(Class<T> clazz, E[] enums, BiFunction<E, DependencyInjector, T> toItems){
        for (E e: enums){
            items.add(
                new ItemWrapper<>(
                    clazz,
                    di -> toItems.apply(e, di)
                )
            );
        }

        return this;
    }

    @Override
    public DependencyInjectorImpl refresh(Consumer<DependencyInjector> func){
        staticItems.add(func);

        return this;
    }

    @Override
    public DependencyInjectorImpl blacklist(Predicate<MultiDep> predicate){
        blackList.add(predicate);

        return this;
    }

    public DependencyInjector initInjector(){
        return new Injector(false);
    }

    public DependencyInjector initInjectorIgnoreErrors(){
        return new Injector(true);
    }

    private class Injector implements DependencyInjector{
        final Deque<Class<?>> debugStack = new ArrayDeque<>();

        private final boolean ignoreErroredItem;

        Injector(boolean ignoreErroredItem){
            this.ignoreErroredItem = ignoreErroredItem;
        }

        @Override
        public synchronized <T> T get(Class<T> clazz){
            T result = null;

            for (ItemWrapper<?> item: items){
                if (!item.isRequested(clazz)) continue;

                T innerItem = (T)item.getInner(this, ignoreErroredItem, debugStack);

                if (innerItem == null) continue;

                if (result != null){
                    throw new RuntimeException("there must only be one instance of " + clazz);
                }

                result = innerItem;
            }

            if (result == null){
                throw new RuntimeException("there is not instance of " + clazz);
            }

            return result;
        }


        @Override
        public synchronized <T extends MultiDep> List<T> getAll(Class<T> clazz){
            List<T> result = new ArrayList<>();

            for (ItemWrapper<?> item: items){
                if (!item.isRequested(clazz)) continue;

                @Nullable
                T innerItem = (T)item.getInner(this, ignoreErroredItem, debugStack);

                if (innerItem == null) continue;

                if (isBlackListed(innerItem)) continue;

                result.add(innerItem);
            }

            return result;
        }

        @Override
        public synchronized void refreshStaticItems() {
            staticItems.forEach(p ->
                p.accept(this)
            );
        }

        private boolean isBlackListed(MultiDep item){
            for (Predicate<MultiDep> blackList: blackList){
                if (blackList.test(item)) return true;
            }
            return false;
        }
    }
}
