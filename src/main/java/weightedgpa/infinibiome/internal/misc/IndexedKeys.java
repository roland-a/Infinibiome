package weightedgpa.infinibiome.internal.misc;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class IndexedKeys {
    private boolean tableInit = false;

    private final AtomicInteger keyIndexCounter = new AtomicInteger();

    public IndexedKeys() {}

    public final class Key<I, T>{
        private final int index;

        private final Function<I, ? extends T> toDefault;

        public Key(Function<I, ? extends T> toDefault) {
            Validate.isTrue(
                !tableInit,
                "key must not be initialized after a table has been created"
            );

            this.toDefault = toDefault;
            this.index = keyIndexCounter.getAndIncrement();
        }

        @Nullable
        private T getDefault(I input){
            return toDefault.apply(input);
        }
    }

    public final class Table {
        private final Object[] values;

        public Table() {
            tableInit = true;

            this.values = new Object[keyIndexCounter.get()];
        }

        public Table(Table original) {
            tableInit = true;

            this.values = original.values.clone();
        }

        @Nullable
        public <T, I> T getValue(Key<I, T> key, I defaultInitializer){
            @SuppressWarnings("unchecked")
            T result = (T)values[key.index];

            if (result == null){
                result = key.getDefault(defaultInitializer);

                values[key.index] = result;
            }
            return result;
        }

        public <T> void setValue(Key<?, T> key, T input){
            values[key.index] = input;
        }
    }
}
