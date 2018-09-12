/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
