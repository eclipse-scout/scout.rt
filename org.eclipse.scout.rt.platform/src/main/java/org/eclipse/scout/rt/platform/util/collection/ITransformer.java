/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.platform.util.collection;

/**
 * <h3>{@link ITransformer}</h3>
 * <p>
 * Interface to transform from one element to another and back.
 *
 * @see TransformingSet
 */
public interface ITransformer<F, T> {
  /**
   * Transforms the specified element to the target type
   *
   * @param x
   *          The input element
   * @return The transformed value in the target type.
   */
  T transform(F x);

  /**
   * Reverts the transformation.
   *
   * @param y
   *          The transformed element in the target type.
   * @return The reverted input element.
   */
  F revert(T y);
}
