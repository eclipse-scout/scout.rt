package org.eclipse.scout.rt.platform.dataobject.value;

import org.eclipse.scout.rt.platform.dataobject.DoNode;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

/**
 * Typed value wrapped.
 */
public interface IValueDo<T> extends IDoEntity {

  String VALUE_ATTRIBUTE = "value";

  DoNode<T> value();

  /**
   * Convenience accessor for the wrapped value. Same as <code>value().get()</code>.
   */
  default T unwrap() {
    return value().get();
  }
}
