package weightedgpa.infinibiome.internal.misc;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;

public final class IndexedKeysInt {
    private boolean tableInit = false;

    private final AtomicInteger keyIndexCounter = new AtomicInteger();

    public IndexedKeysInt() {}

    public final class Key<I>{
        private final int index;

        private final ToIntFunction<I> toDefault;

        public Key(ToIntFunction<I> toDefault) {
            Validate.isTrue(
                !tableInit,
                "key must not be initialized after a table has been created"
            );

            this.toDefault = toDefault;
            this.index = keyIndexCounter.getAndIncrement();
        }

        private int getDefault(I input){
            return toDefault.applyAsInt(input);
        }
    }

    public final class Table {
        private final int[] values;

        public Table() {
            tableInit = true;

            this.values = new int[keyIndexCounter.get()];
        }

        public Table(Table original) {
            tableInit = true;

            this.values = original.values.clone();
        }

        public <I> int getValue(Key<I> key, I defaultInitializer){
            int result = values[key.index];

            if (result == Integer.MIN_VALUE){
                result = key.getDefault(defaultInitializer);

                values[key.index] = result;
            }
            return result;
        }

        public void setValue(Key<?> key, int input){
            values[key.index] = input;
        }
    }
}
