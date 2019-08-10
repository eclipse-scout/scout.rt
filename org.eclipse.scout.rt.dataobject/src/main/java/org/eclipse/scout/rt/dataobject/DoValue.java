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

import java.util.function.Consumer;

/**
 * Wrapper for a generic value of type {@code V} inside a {@link DoEntity} object.
 *
 * @see DoEntity#doValue(String) creator method
 */
public final class DoValue<V> extends DoNode<V> {

  public DoValue() {
    this(null, null);
  }

  protected DoValue(String attributeName, Consumer<DoNode<V>> lazyCreate) {
    super(attributeName, lazyCreate, null);
  }

  public static <V> DoValue<V> of(V value) {
    DoValue<V> doValue = new DoValue<>();
    doValue.set(value);
    return doValue;
  }

  @Override
  public String toString() {
    return "DoValue [m_value=" + get() + " exists=" + exists() + "]";
  }
}
