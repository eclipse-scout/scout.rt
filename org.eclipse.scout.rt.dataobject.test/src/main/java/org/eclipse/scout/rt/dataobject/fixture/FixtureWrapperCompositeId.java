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

@IdTypeName("scout.FixtureWrapperCompositeId")
public final class FixtureWrapperCompositeId extends AbstractCompositeId {
  private static final long serialVersionUID = 1L;

  private FixtureWrapperCompositeId(FixtureCompositeId c1, FixtureStringId c2) {
    super(c1, c2);
  }

  public static FixtureWrapperCompositeId of(FixtureCompositeId c1, FixtureStringId c2) {
    if (c1 == null || c2 == null) {
      return null;
    }
    return new FixtureWrapperCompositeId(c1, c2);
  }

  @RawTypes
  public static FixtureWrapperCompositeId of(String c1, UUID c2, String c3) {
    if (StringUtility.isNullOrEmpty(c1) || c2 == null || StringUtility.isNullOrEmpty(c3)) {
      return null;
    }
    return of(FixtureCompositeId.of(c1, c2), FixtureStringId.of(c3));
  }
}
