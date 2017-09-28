/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.holders;

import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * @since 3.0
 */

public final class HolderUtility {

  private HolderUtility() {
  }

  public static boolean containEqualValues(IHolder a, IHolder b) {
    Object va = a.getValue();
    Object vb = b.getValue();
    return ObjectUtility.equals(va, vb);
  }

  public static <T> void setAndCastValue(IHolder<T> h, Object value) {
    if (h == null) {
      /* nop */
    }
    else {
      h.setValue(TypeCastUtility.castValue(value, h.getHolderType()));
    }
  }

  /**
   * when passing IHolder values to remote services it is necessary to create a value copy of the holder for
   * serialization
   */
  public static <T> IHolder<T> createSerializableHolder(IHolder<T> h) {
    if (h == null) {
      return null;
    }
    return new Holder<>(h.getHolderType(), h.getValue());
  }

}
