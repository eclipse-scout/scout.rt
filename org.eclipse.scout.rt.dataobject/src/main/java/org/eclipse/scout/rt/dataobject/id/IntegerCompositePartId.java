/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import java.io.Serial;

/**
 * Simple wrapper implementation wrapping a {@link Integer} value.
 * <p>
 * <b>Note:</b> Use this class to wrap a component value of an {@link ICompositeId} which is only used within
 * the composite id and is not directly referenced in APIs or persisted data objects. This class doesn't have
 * a distinct {@link IdTypeName} and may therefore not be addressed in specific data object migrations.
 */
@IdSignature(false)
public final class IntegerCompositePartId extends AbstractRootId<Integer> {
  @Serial
  private static final long serialVersionUID = 1L;

  private IntegerCompositePartId(Integer id) {
    super(id);
  }

  public static IntegerCompositePartId of(Integer value) {
    if (value == null || value.intValue() == 0) {
      return null;
    }
    return new IntegerCompositePartId(value);
  }
}
