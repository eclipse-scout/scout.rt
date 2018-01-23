package org.eclipse.scout.rt.platform.dataobject;

import java.util.function.Consumer;

/**
 * Wrapper for a generic value of type {@code V} inside a {@link DoEntity} object.
 *
 * @see DoEntity#doValue(String) creator method
 */
public final class DoValue<V> extends DoNode<V> {

  public DoValue() {
    this(null);
  }

  protected DoValue(Consumer<DoNode<V>> lazyCreate) {
    super(lazyCreate, null);
  }

  @Override
  public String toString() {
    return "DoValue [m_value=" + get() + "]";
  }
}
