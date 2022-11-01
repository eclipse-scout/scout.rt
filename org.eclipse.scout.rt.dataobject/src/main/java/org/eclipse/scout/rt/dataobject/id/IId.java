/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import java.io.Serializable;

/**
 * An interface to represent an arbitrary id. The type of the raw (wrapped) id is not further specified. See various
 * sub-interfaces for commonly used wrapped id types.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(String)</b>: used by {@link IdFactory} to construct new instances. The method is expected to return
 * <code>null</code> if the given {@link String} is <code>null</code>, otherwise the wrapped value.
 * </ul>
 * <p>
 *
 * @see IUuId
 * @see IStringId
 * @see ILongId
 */
public interface IId extends Serializable {

  /**
   * @return the raw id. Use this method carefully. The value of the id should only be used by serialization and
   *         persistence layers.
   */
  Object unwrap();

  /**
   * @return the raw id formatted as string.
   */
  String unwrapAsString();
}
