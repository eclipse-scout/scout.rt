/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
