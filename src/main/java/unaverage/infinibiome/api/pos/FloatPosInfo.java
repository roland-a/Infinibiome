package weightedgpa.infinibiome.api.pos;

public interface FloatPosInfo<T> extends PosInfo<T> {
    T build(double x, double z);

    @Override
    default T build(int x, int z) {
        return build((double)x, (double)z);
    }
}
