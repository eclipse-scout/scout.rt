/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.id.AbstractBooleanId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;

@IdTypeName("scout.FixtureBooleanId")
public final class FixtureBooleanId extends AbstractBooleanId {
  private static final long serialVersionUID = 1L;

  private FixtureBooleanId(Boolean id) {
    super(id);
  }

  public static FixtureBooleanId of(Boolean Boolean) {
    if (Boolean == null) {
      return null;
    }
    return new FixtureBooleanId(Boolean);
  }
}
