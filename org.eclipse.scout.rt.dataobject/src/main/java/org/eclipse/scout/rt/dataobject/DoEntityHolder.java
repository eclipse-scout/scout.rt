/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import org.eclipse.scout.rt.platform.holders.IHolder;

/**
 * Serializable {@link IHolder} implementation for {@link DoEntity} subclasses.
 * <p>
 * To hold less specific types like <code>DoList</code>, use the base class.
 *
 * @see DataObjectHolder
 */
public class DoEntityHolder<T extends IDoEntity> extends DataObjectHolder<T> {

  private static final long serialVersionUID = 1L;

  public DoEntityHolder() {
    this(null);
  }

  public DoEntityHolder(Class<T> clazz) {
    this(clazz, null);
  }

  public DoEntityHolder(Class<T> clazz, T value) {
    super(clazz, value);
  }
}
