/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.id.AbstractLongId;

public final class FixtureLongId extends AbstractLongId {
  private static final long serialVersionUID = 1L;

  private FixtureLongId(Long id) {
    super(id);
  }

  public static FixtureLongId of(Long id) {
    if (id == null || id.longValue() == 0L) {
      return null;
    }
    return new FixtureLongId(id);
  }

  public static FixtureLongId of(String id) {
    if (id == null) {
      return null;
    }
    return new FixtureLongId(Long.parseLong(id));
  }
}
