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

import java.util.function.Function;

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Convenience methods for working with {@link IId}s.
 */
public final class IIds {

  private IIds() {
  }

  /**
   * @return wrapped {@code id} as string representation for logging purpose
   */
  public static String toString(IRootId id) {
    if (id == null) {
      return null;
    }
    return id.unwrapAsString();
  }

  /**
   * Creates a new wrapped {@link IId} by calling the {@code of(value(s))} method of the given id class.
   */
  public static <ID extends IId> ID create(Class<ID> idClass, Object... values) {
    return BEANS.get(IdFactory.class).createInternal(idClass, values);
  }

  /**
   * Returns a function to create new {@link IRootId} of the provided type. Bean lookup to {@link IdFactory} is cached.
   */
  public static <ID extends IRootId, WT> Function<WT, ID> factory(Class<ID> idClass) {
    final IdFactory idFactory = BEANS.get(IdFactory.class);
    return value -> idFactory.createInternal(idClass, value);
  }
}
