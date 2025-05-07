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

@IdTypeName("scout.FixtureComposite3Id")
public final class FixtureComposite3Id extends AbstractCompositeId {
  private static final long serialVersionUID = 1L;

  private FixtureComposite3Id(FixtureLongId c1, FixtureLongId c2, FixtureStringId c3) {
    super(c1, c2, c3);
  }

  @RawTypes
  public static FixtureComposite3Id of(Long c1, Long c2, String c3) {
    if (c1 == null || c2 == null || StringUtility.isNullOrEmpty(c3)) {
      return null;
    }
    return new FixtureComposite3Id(FixtureLongId.of(c1), FixtureLongId.of(c2), FixtureStringId.of(c3));
  }

  public static FixtureComposite3Id of(FixtureLongId c1, FixtureLongId c2, FixtureStringId c3) {
    if (c1 == null || c2 == null) {
      return null;
    }
    return new FixtureComposite3Id(c1, c2, c3);
  }
}
