/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
