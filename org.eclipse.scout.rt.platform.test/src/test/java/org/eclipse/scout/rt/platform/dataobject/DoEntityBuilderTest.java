/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertEqualsWithComparisonFailure;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

public class DoEntityBuilderTest {

  @Test
  public void testBuild() {
    DoEntity expected = BEANS.get(DoEntity.class);
    expected.put("attribute1", "foo");
    expected.put("attribute2", 42);
    expected.put("attribute3", "bar");
    expected.putList("listAttribute1", CollectionUtility.arrayList(1, 2, 3));
    expected.putList("listAttribute2", CollectionUtility.arrayList(4, 5, 6));

    DoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("attribute1", "foo")
        .put("attribute2", 42)
        .putIf("attribute3", "bar", Objects::nonNull)
        .putIf("attribute4", null, Objects::nonNull)
        .putList("listAttribute1", 1, 2, 3)
        .putList("listAttribute2", Arrays.asList(4, 5, 6))
        .build();
    assertEqualsWithComparisonFailure(expected, actual);

    // ensure lists are mutable
    expected.getList("listAttribute1").remove(1);
    actual.getList("listAttribute1").remove(1);
    assertEqualsWithComparisonFailure(expected, actual);
  }

  @Test
  public void testBuildNullValues() {
    DoEntity expected = BEANS.get(DoEntity.class);
    expected.put("attribute1", null);
    expected.putList("listAttribute1", CollectionUtility.emptyArrayList());
    expected.putList("listAttribute2", CollectionUtility.emptyArrayList());

    DoEntity actual = BEANS.get(DoEntityBuilder.class)
        .put("attribute1", null)
        .putList("listAttribute1", (Object[]) null)
        .putList("listAttribute2", (List<Object>) null)
        .build();
    assertEqualsWithComparisonFailure(expected, actual);
  }
}
