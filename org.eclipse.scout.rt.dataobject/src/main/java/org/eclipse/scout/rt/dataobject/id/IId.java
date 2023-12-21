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

import java.io.Serializable;

/**
 * An interface to represent an arbitrary id. The type of the raw (wrapped) id is not further specified. See various
 * sub-interfaces for commonly used wrapped id types.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(wrapped_type(s)></b>: used by {@link IdFactory} to construct new instances. The method is expected to
 * return <code>null</code> if the given {@code value(s)} are <code>null</code>, otherwise the wrapped value.
 * </ul>
 * <p>
 *
 * @see IRootId
 * @see ICompositeId
 */
@IdSignature
public interface IId extends Serializable {

  /**
   * @return the raw id. Use this method carefully. The value of the id should only be used by serialization and
   *         persistence layers.
   */
  Object unwrap();
}
