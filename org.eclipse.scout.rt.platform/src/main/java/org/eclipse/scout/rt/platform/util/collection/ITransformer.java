/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
