package weightedgpa.infinibiome.internal.dependency;

import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;


final class ItemWrapper<T>{
    private final Class<? extends T> clazz;
    private final Function<DependencyInjector, T> toItemFunc;

    @Nullable
    private T finished = null;

    private boolean errored = false;

    ItemWrapper(Class<T> clazz, Function<DependencyInjector, T> toItemFunc) {
        this.clazz = clazz;
        this.toItemFunc = toItemFunc;
    }

    boolean isRequested(Class<?> requestedClass){
        return requestedClass.isAssignableFrom(clazz);
    }

    //returns null only if item errored and ignoreError is true
    @Nullable
    synchronized T getInner(DependencyInjector di, boolean ignoreError, Deque<Class<?>> debugStack){
        if (debugStack.contains(clazz)){
            throw new RuntimeException("circular dependency detected: " + debugStack);
        }

        if (finished == null && !errored){
            debugStack.push(clazz);

            try{
                finished = toItemFunc.apply(di);
            }
            catch (Throwable e){
                if (ignoreError){
                    finished = null;
                    errored = true;
                }
                else {
                    throw e;
                }
            }
            finally {
                debugStack.pop();
            }
        }
        return finished;
    }

    @Override
    public String toString() {
        return "Item{" +
            "clazz=" + clazz.getSimpleName() + ", " +
            "id=" + Integer.toHexString(System.identityHashCode(this)) +
            '}';
    }
}
