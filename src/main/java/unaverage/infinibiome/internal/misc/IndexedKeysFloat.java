package weightedgpa.infinibiome.internal.misc;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public final class IndexedKeysFloat {
    private boolean tableInit = false;

    private final AtomicInteger keyIndexCounter = new AtomicInteger();

    public IndexedKeysFloat() {}

    public final class Key<I>{
        private final int index;

        private final ToDoubleFunction<I> toDefault;

        public Key(ToDoubleFunction<I> toDefault) {
            Validate.isTrue(
                !tableInit,
                "key must not be initialized after a table has been created"
            );

            this.toDefault = toDefault;
            this.index = keyIndexCounter.getAndIncrement();
        }

        private double getDefault(I input){
            return toDefault.applyAsDouble(input);
        }
    }

    public final class Table {
        private final float[] values;

        public Table() {
            tableInit = true;

            this.values = new float[keyIndexCounter.get()];
        }

        public Table(Table original) {
            tableInit = true;

            this.values = original.values.clone();
        }

        public <I> double getValue(Key<I> key, I defaultInitializer){
            double result = values[key.index];

            if (Double.isNaN(result)){
                result = key.getDefault(defaultInitializer);

                values[key.index] = (float) result;
            }
            return result;
        }

        public <T> void setValue(Key<?> key, double input){
            values[key.index] = (float) input;
        }
    }
}
