package org.eclipse.scout.rt.platform.util.concurrent;

/**
 * Represents an operation that accepts one input arguments and returns no result.
 * <p>
 * This is a surrogate for <code>Consumer</code> contained in Java 8, and is to comply with Java 7. This interface will
 * be removed once compiler compliance level is changed to 1.8.
 *
 * @param <T>
 *          the type of the first argument to the operation
 */
public interface IConsumer<T> {

  /**
   * Performs this operation on the given arguments.
   *
   * @param t
   *          the input argument
   */
  void accept(T t);
}
