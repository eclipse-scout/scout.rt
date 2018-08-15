package org.eclipse.scout.rt.platform.dataobject;

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
