package org.eclipse.scout.rt.platform.util.concurrent;

/**
 * Represents a function that accepts two arguments and produces a result. This is the two-arity specialization of
 * <code>Function</code>.
 * <p>
 * This is a surrogate for <code>BiConsumer</code> contained in Java 8, and is to comply with Java 7. This interface
 * will be removed once compiler compliance level is changed to 1.8.
 *
 * @param <T>
 *          the type of the first argument to the function
 * @param <U>
 *          the type of the second argument to the function
 * @param <R>
 *          the type of the result of the function
 */
public interface IBiFunction<T, U, R> {

  /**
   * Applies this function to the given argument.
   */
  R apply(T t, U u);
}
