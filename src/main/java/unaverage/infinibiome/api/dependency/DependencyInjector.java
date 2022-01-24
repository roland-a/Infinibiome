package weightedgpa.infinibiome.api.dependency;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Used for retrieving objects that objects may depend on
 *
 * Can either retrieve a single object, or every object of a specific class
 */
public interface DependencyInjector {
    /**
     * Gets a single object
     * That single object will get all of its dependencies, and its dependencies will get their dependencies, and so on.
     *
     ** @throws StackOverflowError
     * If there are any circular dependencies
     */
    <T> T get(Class<T> clazz);

    /**
     * Returns every instances of T
     * Each instance will get all of its dependencies, and its dependencies will get their dependencies, and so on.
     *
     * @throws StackOverflowError
     * If there are any circular dependencies
     */
    <T extends MultiDep> List<T> getAll(Class<T> clazz);

    void refreshStaticItems();

    interface Pre {
        /**
         * Adds an item to the dependency injector
         */
        <T> Pre addItem(Class<T> clazz, Function<DependencyInjector, T> toItem);

        /**
         * Adds multiple items to the dependency injector that depend on an enum value
         */
        <T, E> Pre addItems(Class<T> clazz, E[] enums, BiFunction<E, DependencyInjector, T> toItems);

        /**
         * Prevents an object from being retrieved by the DI
         * Useful for replacing said objects with your own
         */
        Pre blacklist(Predicate<MultiDep> predicate);

        Pre refresh(Consumer<DependencyInjector> func);
    }
}
