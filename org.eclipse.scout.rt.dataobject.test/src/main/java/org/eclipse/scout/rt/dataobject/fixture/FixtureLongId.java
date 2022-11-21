/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
