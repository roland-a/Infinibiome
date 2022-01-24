package weightedgpa.infinibiome.internal.misc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Predicate;

public final class EntityHelper {
    private static final Field GOALS_FIELD = ObfuscationReflectionHelper.findField(
        GoalSelector.class,
        "field_220892_d"
    );

    private EntityHelper() {}

    public static void removeGoals(
        GoalSelector goalSelector,
        Predicate<Goal> goalRemoval
    ){

        getGoalSet(goalSelector).removeIf(g -> goalRemoval.test(g.getGoal()));
    }

    static void removeGoals(
        GoalSelector goalSelector,
        int priority
    ){
        getGoalSet(goalSelector).removeIf(g -> g.getPriority() == priority);
    }

    static void shiftGoalsDown(int priority, GoalSelector goalSelector){
        Set<PrioritizedGoal> goals = getGoalSet(goalSelector);

        for (PrioritizedGoal goal: new ArrayList<>(goals)){
            if (goal.getPriority() >= priority){
                goals.remove(goal);

                goals.add(
                    new PrioritizedGoal(
                        goal.getPriority() + 1,
                        goal.getGoal()
                    )
                );
            }
        }
    }

    private static Set<PrioritizedGoal> getGoalSet(GoalSelector goal){
        try {
            return (Set<PrioritizedGoal>)GOALS_FIELD.get(goal);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean inVillage(BlockPos pos, IWorld world){
        return !world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES).getStructureReferences(Feature.VILLAGE.getStructureName()).isEmpty();
    }

    static void applyModifier(
        LivingEntity entity,
        IAttribute attribute,
        AttributeModifier modifier
    ) {
        if (entity.getAttribute(attribute).hasModifier(modifier)) return;

        entity.getAttribute(attribute).applyModifier(modifier);
    }

    static void removeModifier(
        LivingEntity entity,
        IAttribute attribute,
        AttributeModifier modifier
    ) {
        if (!entity.getAttribute(attribute).hasModifier(modifier)) return;

        entity.getAttribute(attribute).removeModifier(modifier);
    }
}
