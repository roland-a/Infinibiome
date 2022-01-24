package weightedgpa.infinibiome.internal.misc;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public final class IndexedKeysBool {
    private boolean tableInit = false;

    private final AtomicInteger keyIndexCounter = new AtomicInteger();

    public IndexedKeysBool() {}

    public final class Key<I>{
        private final int index;

        private final Predicate<I> toDefault;

        public Key(Predicate<I> toDefault) {
            Validate.isTrue(
                !tableInit,
                "key must not be initialized after a table has been created"
            );

            this.toDefault = toDefault;
            this.index = keyIndexCounter.getAndIncrement();
        }

        private boolean getDefault(I input){
            return toDefault.test(input);
        }
    }

    public final class Table {
        private final boolean[] values;
        private final boolean[] isSet;

        public Table() {
            tableInit = true;

            this.values = new boolean[keyIndexCounter.get()];
            this.isSet = new boolean[keyIndexCounter.get()];
        }

        public Table(Table original) {
            tableInit = true;

            this.values = original.values.clone();
            this.isSet = original.isSet.clone();
        }

        public <I> boolean getValue(Key<I> key, I defaultInitializer){
            boolean result = values[key.index];

            if (!isSet[key.index]){
                result = key.getDefault(defaultInitializer);

                values[key.index] = result;
            }
            return result;
        }

        public void setValue(Key<?> key, boolean input){
            values[key.index] = input;
        }
    }
}
