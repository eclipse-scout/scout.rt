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

/**
 * An interface to represent a {@code Long}-based id.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(Long)</b>: used by {@link IdFactory} to construct new instances. The method is expected to return
 * <code>null</code> if the given {@link Long} is <code>null</code>, otherwise the wrapped {@link Long}.
 * </ul>
 * This interface implements {@link Comparable} by comparing the wrapped {@link Long} value, without considering the id
 * types. Hence, every {@link ILongId} is comparable to any other {@link ILongId}.
 */
public interface ILongId extends IRootId, Comparable<ILongId> {

  @Override
  Long unwrap();

  @Override
  default int compareTo(ILongId o) {
    if (o == null) {
      return 1;
    }
    return unwrap().compareTo(o.unwrap());
  }
}
