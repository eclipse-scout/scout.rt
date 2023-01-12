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

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.id.AbstractUuId;

public final class FixtureUuIdWithCustomFromString extends AbstractUuId {
  private static final long serialVersionUID = 1L;

  private FixtureUuIdWithCustomFromString(UUID id) {
    super(id);
  }

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
}
