/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

/**
 * An interface to represent an arbitrary id with cardinality one. The type of the raw (wrapped) id is not further
 * specified. See various sub-interfaces for commonly used wrapped id types.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(wrapped-type)</b>: used by {@link IdFactory} to construct new instances. The method is expected to return
 * <code>null</code> if the given {@code value} is <code>null</code>, otherwise the wrapped value.
 * </ul>
 * <p>
 *
 * @see IUuId
 * @see IStringId
 * @see ILongId
 * @see AbstractRootId
 */
public interface IRootId extends IId {

  /**
   * Returns a string representation of the id. In general, the {@code unwrapAsString} method returns a string that
   * "textually represents" this identifier. The result should be a concise but informative representation that is easy
   * for a person to read.
   * <p>
   * The string representation is useful for logging and debugging purposes. See {@link IIds#toString(IRootId)} for a
   * null-safe implementation.<br>
   * <b>Important:</b> The return value of this method and should never be used as stable serialized representation of
   * this identifier. See {@link IdCodec} for various serialization representations of an {@link IId}.
   *
   * @return a string representation of the id for logging and debugging and purpose.
   * @see IIds#toString(IRootId)
   * @see IdCodec
   */
  String unwrapAsString();
}
