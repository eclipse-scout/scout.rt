/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.id;

import java.io.Serializable;

/**
 * An interface to represent an id. The raw (wrapped) id is of the generic type <code>WRAPPED_TYPE</code>.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(String)</b>: used by {@link IdFactory} to construct new instances. The method is expected to return
 * <code>null</code> if the given {@link String} is <code>null</code>, otherwise the wrapped value.
 * </ul>
 * <p>
 * This interface implements {@link Comparable} by comparing the wrapped value, without considering the id types. Hence
 * every {@link IId} is comparable to any other {@link IId}.
 */
public interface IId<WRAPPED_TYPE extends Comparable<WRAPPED_TYPE>> extends Comparable<IId<WRAPPED_TYPE>>, Serializable {

  /**
   * @return the raw id. Use this method carefully. The value of the id should only be used by serialization and
   *         persistence layers.
   */
  WRAPPED_TYPE unwrap();

  String unwrapAsString();

  @Override
  default int compareTo(IId<WRAPPED_TYPE> o) {
    if (o == null) {
      return 1;
    }
    return unwrap().compareTo(o.unwrap());
  }
}
