/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.id;

import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.id.AbstractIdCodecTest.CustomComparableRawDataType;
import org.eclipse.scout.rt.dataobject.testing.AbstractIdStructureTest;
import org.junit.runners.Parameterized.Parameters;

public class DataObjectIdStructureTest extends AbstractIdStructureTest {

  @Parameters(name = "{0}")
  public static Iterable<?> parameters() {
    return streamIdClasses("org.eclipse.scout.rt.dataobject")
        .filter(id -> !UnknownId.class.isAssignableFrom(id)) // UnknownId does not comply with IId structure
        .collect(Collectors.toList());
  }

  public DataObjectIdStructureTest(Class<? extends IId> idClass) {
    super(idClass);
  }

  @Override
  protected Object getCustomMockValue(Class<?> clazz) {
    if (clazz == CustomComparableRawDataType.class) {
      return CustomComparableRawDataType.of("42");
    }
    return super.getCustomMockValue(clazz);
  }
}
