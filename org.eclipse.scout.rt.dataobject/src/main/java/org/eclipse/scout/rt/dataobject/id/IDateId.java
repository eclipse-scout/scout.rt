/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import java.util.Date;

/**
 * An interface to represent a {@code Date}-based id.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>of(Date)</b>: used by {@link IdFactory} to construct new instances. The method is expected to return
 * <code>null</code> if the given {@link Long} is <code>null</code>, otherwise the wrapped {@link Long}.
 * </ul>
 * This interface implements {@link Comparable} by comparing the wrapped {@link Date} value, without considering the id
 * types. Hence, every {@link IDateId} is comparable to any other {@link IDateId}.
 */
public interface IDateId extends IRootId, Comparable<IDateId> {

  @Override
  Date unwrap();

  @Override
  default int compareTo(IDateId o) {
    if (o == null) {
      return 1;
    }
    return unwrap().compareTo(o.unwrap());
  }
}
