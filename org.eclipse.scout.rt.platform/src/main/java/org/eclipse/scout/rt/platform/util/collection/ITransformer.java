/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.collection;

/**
 * Interface to transform from one element to another and back.
 *
 * @see TransformingSet
 */
public interface ITransformer<F, T> {
  /**
   * Transforms the specified element to the target type
   *
   * @param x
   *     The input element
   * @return The transformed value in the target type.
   */
  T transform(F x);

  /**
   * Reverts the transformation.
   *
   * @param y
   *     The transformed element in the target type.
   * @return The reverted input element.
   */
  F revert(T y);
}
