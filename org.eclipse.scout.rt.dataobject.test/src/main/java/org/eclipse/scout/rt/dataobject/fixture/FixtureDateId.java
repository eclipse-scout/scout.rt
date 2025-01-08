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

import java.util.Date;

import org.eclipse.scout.rt.dataobject.id.AbstractDateId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;

@IdTypeName("scout.FixtureDateId")
public final class FixtureDateId extends AbstractDateId {
  private static final long serialVersionUID = 1L;

  private FixtureDateId(Date id) {
    super(id);
  }

  public static FixtureDateId of(Date date) {
    if (date == null) {
      return null;
    }
    return new FixtureDateId(date);
  }
}
