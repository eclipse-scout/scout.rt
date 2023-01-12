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

import java.util.function.Consumer;

/**
 * Wrapper for a generic value of type {@code V} inside a {@link DoEntity} object.
 *
 * @see DoEntity#doValue(String) creator method
 */
public final class DoValue<V> extends DoNode<V> {

  public DoValue() {
    this(null, null, null);
  }

  DoValue(String attributeName, Consumer<DoNode<V>> lazyCreate, V initialValue) {
    super(attributeName, lazyCreate, initialValue);
  }

  public static <V> DoValue<V> of(V value) {
    return new DoValue<>(null, null, value);
  }

  @Override
  public String toString() {
    return "DoValue [m_value=" + get() + " exists=" + exists() + "]";
  }
}
