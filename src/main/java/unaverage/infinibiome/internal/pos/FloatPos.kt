package weightedgpa.infinibiome.internal.pos


inline class FloatPos(val l: Long){
    constructor(x: Float, z: Float): this(
        (java.lang.Float.floatToRawIntBits(x).toLong() shl 32) or
        (java.lang.Float.floatToRawIntBits(z).toLong() and 0xffffffffL)
    )

    val x: Float get() = java.lang.Float.intBitsToFloat((l shr 32).toInt())
    val z: Float get() = java.lang.Float.intBitsToFloat(l.toInt())

    fun scale(s: Float): FloatPos{
        return FloatPos(
            this.x * (1 / s),
            this.z * (1 / s)
        );
    }
}