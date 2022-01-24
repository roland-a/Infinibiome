package weightedgpa.infinibiome.internal.generators.utils.condition;

import net.minecraft.util.math.ChunkPos;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.Interpolation;
import weightedgpa.infinibiome.internal.misc.MCHelper;

public abstract class Condition {
    public Condition(){}

    public abstract double getProbability(BlockPos2D pos);

    public boolean isSlow(){
        return false;
    }

    public boolean isStrict() {
        return false;
    }

    public double getProbability(ChunkPos pos){
        return getProbability(MCHelper.lowestPos(pos));
    };;

    public boolean likelyConflicts(Condition other){
        return this.getClass() == other.getClass();
    }

    public Condition invert(){
        return new WrapperCondition(this) {
            @Override
            public double getProbability(BlockPos2D pos) {
                return 1 - Condition.this.getProbability(pos);
            }

            @Override
            public double getProbability(ChunkPos pos) {
                return 1 - Condition.this.getProbability(pos);
            }

            @Override
            public String toString() {
                return "Invert{" + getInner() + "}";
            }
        };
    }

    public Condition noConflict(){
        return new WrapperCondition(this) {
            @Override
            public boolean likelyConflicts(Condition other) {
                return false;
            }

            @Override
            public String toString() {
                return "NoConflict{" + getInner() + "}";
            }
        };
    }

    public Condition activeOutside(Condition chooser){
        return ConditionHelper.switchBetweenConditions(
            chooser,
            new Condition(){
                @Override
                public double getProbability(BlockPos2D pos) {
                    return 1;
                }

                @Override
                public String toString() {
                    return "Always1{}";
                }
            },
            this
        );
    }

    public abstract static class WrapperCondition extends Condition{
        private final Condition inner;

        protected WrapperCondition(Condition inner) {
            this.inner = inner;
        }

        @Override
        public double getProbability(BlockPos2D pos) {
            return inner.getProbability(pos);
        }

        @Override
        public double getProbability(ChunkPos pos) {
            return inner.getProbability(pos);
        }

        @Override
        public boolean isSlow() {
            return inner.isSlow();
        }

        @Override
        public boolean isStrict() {
            return inner.isStrict();
        }

        @Override
        public boolean likelyConflicts(Condition other) {
            if (other instanceof WrapperCondition){
                return inner.likelyConflicts(((WrapperCondition)other).getInner());
            }
            return inner.likelyConflicts(other);
        }

        @Override
        public Condition invert() {
            return inner.invert();
        }

        @Override
        public Condition noConflict() {
            return inner.noConflict();
        }

        @Override
        public Condition activeOutside(Condition chooser) {
            return inner.activeOutside(chooser);
        }

        Condition getInner(){
            return inner;
        }
    }

    public abstract static class BoolInterpolated extends Condition {
        private final Interpolation<BlockPos2D> interpolation = new Interpolation<>(
            p -> passes(p) ? 1 : 0,
            16,
            4,
            BlockPos2D.INFO
        );

        public abstract boolean passes(BlockPos2D pos);


        @Override
        public final boolean isStrict() {
            return false;
        }

        @Override
        public double getProbability(BlockPos2D pos) {
            return interpolation.getOutput(pos);
        }

        @Override
        public double getProbability(ChunkPos pos) {
            return passes(MCHelper.lowestPos(pos)) ? 1 : 0;
        }
    }

    public abstract static class BoolStrict extends Condition {
        public abstract boolean passes(BlockPos2D pos);

        @Override
        public final boolean isStrict() {
            return true;
        }

        @Override
        public double getProbability(BlockPos2D pos) {
            return passes(pos) ? 1 : 0;
        }
    }
}
