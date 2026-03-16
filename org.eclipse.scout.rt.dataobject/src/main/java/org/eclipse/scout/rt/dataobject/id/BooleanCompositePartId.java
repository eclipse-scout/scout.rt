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
 * Simple wrapper implementation wrapping a {@link Boolean} value.
 * <p>
 * <b>Note:</b> Use this class to wrap a component value of an {@link ICompositeId} which is only used within
 * the composite id and is not directly referenced in APIs or persisted data objects. This class doesn't have
 * a distinct {@link IdTypeName} and may therefore not be addressed in specific data object migrations.
 */
@IdSignature(false)
public final class BooleanCompositePartId extends AbstractBooleanId {
  @Serial
  private static final long serialVersionUID = 1L;

  private BooleanCompositePartId(Boolean id) {
    super(id);
  }

  public static BooleanCompositePartId of(Boolean value) {
    if (value == null) {
      return null;
    }
    return new BooleanCompositePartId(value);
  }
}
