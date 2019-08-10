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

import org.eclipse.scout.rt.platform.BEANS;

/**
 * Convenience methods for working with {@link IId}s.
 */
public final class IIds {

  private IIds() {
  }

  /**
   * Null-safe version of {@link IId#unwrapAsString()}.
   */
  public static String toString(IId<?> id) {
    if (id == null) {
      return null;
    }
    return id.unwrapAsString();
  }

  /**
   * Creates a new wrapped {@link IId} by calling the <code>of(value)</code> method of the given id class.
   */
  public static <ID extends IId<WT>, WT extends Comparable<WT>> ID create(Class<ID> idClass, WT value) {
    return BEANS.get(IdFactory.class).createInternal(idClass, value);
  }
}
