package weightedgpa.infinibiome.internal.misc;

public final class Log2helper {
    private Log2helper(){}

    public static int asLog2(int v){
        assert v > 0 && (v & (v-1)) == 0;

        return Integer.numberOfTrailingZeros(v);
    }

    public static int toNormal(int lengthLog2){
        return mult(1, lengthLog2);
    }

    public static int mod(int v, int lengthLog2){
        return v & (toNormal(lengthLog2) - 1);
    }

    public static int lowest(int v, int lengthLog2){
        return v & -toNormal(lengthLog2);
    }

    public static int floorDiv(int v, int lengthLog2) {
        return v >> lengthLog2;
    }

    public static int mult(int v, int lengthLog2){
        return v << lengthLog2;
    }

    
}
