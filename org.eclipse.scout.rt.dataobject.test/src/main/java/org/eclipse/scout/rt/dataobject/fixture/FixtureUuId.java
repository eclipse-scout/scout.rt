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
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IdTypeName("scout.FixtureUuId")
public final class FixtureUuId extends AbstractUuId {
  private static final long serialVersionUID = 1L;

  private FixtureUuId(UUID id) {
    super(id);
  }

  public static FixtureUuId create() {
    return new FixtureUuId(UUID.randomUUID());
  }

  public static FixtureUuId of(UUID id) {
    if (id == null) {
      return null;
    }
    return new FixtureUuId(id);
  }

  public static FixtureUuId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new FixtureUuId(UUID.fromString(id));
  }
}
