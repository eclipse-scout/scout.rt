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

import org.eclipse.scout.rt.dataobject.id.AbstractCompositeId;
import org.eclipse.scout.rt.dataobject.id.BooleanCompositePartId;
import org.eclipse.scout.rt.dataobject.id.DateCompositePartId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.dataobject.id.IntegerCompositePartId;
import org.eclipse.scout.rt.dataobject.id.LongCompositePartId;
import org.eclipse.scout.rt.dataobject.id.RawTypes;

@IdTypeName("scout.FixturePartsCompositeId")
public final class FixturePartsCompositeId extends AbstractCompositeId {
  private static final long serialVersionUID = 1L;

  private FixturePartsCompositeId(IntegerCompositePartId p1, DateCompositePartId p2, LongCompositePartId p3, BooleanCompositePartId p4) {
    super(p1, p2, p3, p4);
  }

  @RawTypes
  public static FixturePartsCompositeId of(Integer p1, Date p2, Long p3, Boolean p4) {
    if (p1 == null && p2 == null && p3 == null && p4 == null) {
      return null;
    }
    return new FixturePartsCompositeId(IntegerCompositePartId.of(p1), DateCompositePartId.of(p2), LongCompositePartId.of(p3), BooleanCompositePartId.of(p4));
  }

  public static FixturePartsCompositeId of(IntegerCompositePartId p1, DateCompositePartId p2, LongCompositePartId p3, BooleanCompositePartId p4) {
    if (p1 == null && p2 == null && p3 == null && p4 == null) {
      return null;
    }
    return new FixturePartsCompositeId(p1, p2, p3, p4);
  }
}
