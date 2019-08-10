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

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.id.AbstractUuId;

public final class FixtureUuIdWithCustomFromString extends AbstractUuId {
  private static final long serialVersionUID = 1L;

  public static FixtureUuIdWithCustomFromString create() {
    return new FixtureUuIdWithCustomFromString(UUID.randomUUID());
  }

  public static FixtureUuIdWithCustomFromString of(UUID id) {
    if (id == null) {
      return null;
    }
    return new FixtureUuIdWithCustomFromString(id);
  }

  public static FixtureUuIdWithCustomFromString of(String s) {
    if (s == null) {
      return null;
    }
    return new FixtureUuIdWithCustomFromString(UUID.fromString(s.replace('.', '-')));
  }

  private FixtureUuIdWithCustomFromString(UUID id) {
    super(id);
  }
}
