package org.eclipse.scout.rt.platform.util.concurrent;

/**
 * Represents an operation that accepts two input arguments and returns no result. This is the two-arity specialization
 * of <code>Consumer</code>.
 * <p>
 * This is a surrogate for <code>BiConsumer</code> contained in Java 8, and is to comply with Java 7.
 *
 * @param <T>
 *          the type of the first argument to the operation
 * @param <U>
 *          the type of the second argument to the operation
 */
public interface IBiConsumer<T, U> {

  /**
   * Performs this operation on the given arguments.
   *
   * @param t
   *          the first input argument
   * @param u
   *          the second input argument
   */
  void accept(T t, U u);
}
