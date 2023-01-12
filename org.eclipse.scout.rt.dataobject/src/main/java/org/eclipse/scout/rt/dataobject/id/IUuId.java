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

import java.util.UUID;

/**
 * An interface to represent the unique universal id of an object. The raw (wrapped) id is of type {@link UUID}.
 * <p>
 * Subclasses must provide the following static factory methods:
 * <ul>
 * <li><b>create()</b>: used to create new ids (like a database sequence). The method is expected to return a new
 * instance wrapping {@link UUID#randomUUID()}.
 * <li><b>of(UUID)</b>: used by {@link IdFactory} to construct new instances. The method is expected to return
 * <code>null</code> if the given {@link UUID} is <code>null</code>, otherwise the wrapped {@link UUID}.
 * </ul>
 * This interface implements {@link Comparable} by comparing the wrapped {@link UUID} value, without considering the id
 * types. Hence, every {@link IUuId} is comparable to any other {@link IUuId}.
 */
public interface IUuId extends IRootId, Comparable<IUuId> {

  @Override
  UUID unwrap();

  @Override
  default int compareTo(IUuId o) {
    if (o == null) {
      return 1;
    }
    return unwrap().compareTo(o.unwrap());
  }
}
