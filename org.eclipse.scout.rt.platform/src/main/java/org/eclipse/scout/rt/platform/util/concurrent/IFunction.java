package org.eclipse.scout.rt.platform.util.concurrent;

/**
 * Represents a function that accepts one argument and produces a result.
 * <p>
 * This is a surrogate for <code>Function</code> contained in Java 8, and is to comply with Java 7. This interface will
 * be removed once compiler compliance level is changed to 1.8.
 *
 * @param <T>
 *          the type of the input to the function
 * @param <R>
 *          the type of the result of the function
 */
public interface IFunction<T, R> {

  /**
   * Applies this function to the given argument.
   *
   * @param t
   *          the function argument
   * @return the function result
   */
  R apply(T t);
}
