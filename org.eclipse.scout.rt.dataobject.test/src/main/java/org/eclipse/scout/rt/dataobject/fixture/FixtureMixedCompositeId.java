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

import org.eclipse.scout.rt.dataobject.id.AbstractCompositeId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.dataobject.id.RawTypes;
import org.eclipse.scout.rt.platform.util.StringUtility;

@IdTypeName("scout.FixtureMixedCompositeId")
public final class FixtureMixedCompositeId extends AbstractCompositeId {
  private static final long serialVersionUID = 1L;

  private FixtureMixedCompositeId(FixtureStringId c1, UUID c2) {
    super(c1, c2);
  }

  @RawTypes
  public static FixtureMixedCompositeId of(String c1, UUID c2) {
    if (StringUtility.isNullOrEmpty(c1) || c2 == null) {
      return null;
    }
    return new FixtureMixedCompositeId(FixtureStringId.of(c1), c2);
  }

  public static FixtureMixedCompositeId of(FixtureStringId c1, UUID c2) {
    if (c1 == null || c2 == null) {
      return null;
    }
    return new FixtureMixedCompositeId(c1, c2);
  }

  public FixtureStringId getStringId() {
    return idComponent(0);
  }

  public UUID getUUID() {
    return idComponent(1);
  }
}
