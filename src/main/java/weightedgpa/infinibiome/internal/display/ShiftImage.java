package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.internal.misc.Helper;

import java.util.function.Function;
import java.util.function.Supplier;

final class ShiftImage<T>{
    private final int xLength;
    private final int zLength;
    private final NewFunc<? extends T> newFunc;

    private int xOffset = 0;
    private int zOffset = 0;

    private Array<Array<T>> rowColumn;

    ShiftImage(int xLength, int zLength, NewFunc<? extends T> newFunc) {
        this.xLength = xLength;
        this.zLength = zLength;
        this.newFunc = newFunc;

        rowColumn = new Array<>(
            xLength,
            i -> new Array<T>(zLength)
        );
    }

    interface NewFunc<T>{
        T produce(int x, int z);
    }

    public void shift(int xOffset, int zOffset){
        this.xOffset += xOffset;
        this.zOffset += zOffset;

        shift(rowColumn, xOffset, () -> new Array<>(zLength));

        for (int i = 0; i < xLength; i++){
            shift(rowColumn.get(i), zOffset, () -> null);
        }
    }

    int getXOffset() {
        return xOffset;
    }

    int getZOffset() {
        return zOffset;
    }

    void iter(Consumer<? super T> iterFunc){
        for (int x = 0; x < xLength; x++){
            for (int z = 0; z < zLength; z++){
                T item = rowColumn.get(x).get(z);

                if (item == null){
                    item = newFunc.produce(x + xOffset, z + zOffset);

                    rowColumn.get(x).set(z, item);
                }

                iterFunc.accept(x, z, item);
            }
        }
    }

    void iterParallel(Consumer<? super T> iterFunc){
        Helper.iterXZParallel(
            xLength,
            zLength,
            (x, z) -> {
                T item = rowColumn.get(x).get(z);

                if (item == null){
                    item = newFunc.produce(x + xOffset, z + zOffset);

                    rowColumn.get(x).set(z, item);
                }

                iterFunc.accept(x, z, item);
            }
        );
    }

    private static <T> void shift(Array<T> original, int offset, Supplier<T> newT){
        final Array<T> result = new Array<>(original.getLength());

        for (int i = 0; i < original.getLength(); i++){
            final int offsetI = i + offset;

            if (offsetI < 0 || offsetI >= original.getLength()){
                result.set(i, newT.get());
            }
            else {
                result.set(i, original.get(offsetI));
            }
        }

        for (int i = 0; i < original.getLength(); i++){
            original.set(i, result.get(i));
        }
    }

    @FunctionalInterface
    interface Consumer<T>{
        void accept(int x, int z, T item);
    }

    private static class Array<T>{
        final Object[] arr;

        Array(int length) {
            this.arr = new Object[length];
        }

        Array(int length, Function<Integer, T> toDefault) {
            this.arr = new Object[length];

            for (int i = 0; i < arr.length; i++){
                arr[i] = toDefault.apply(i);
            }
        }

        int getLength(){
            return arr.length;
        }

        @SuppressWarnings("unchecked")
        T get(int index){
            return (T)arr[index];
        }

        void set(int index, T item){
            arr[index] = item;
        }
    }
}


