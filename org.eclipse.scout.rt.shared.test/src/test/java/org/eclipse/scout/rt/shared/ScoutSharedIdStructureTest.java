/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared;

import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.testing.AbstractIdStructureTest;
import org.junit.runners.Parameterized.Parameters;

public class ScoutSharedIdStructureTest extends AbstractIdStructureTest {

  @Parameters(name = "{0}")
  public static Iterable<?> parameters() {
    return streamIdClasses("org.eclipse.scout.rt.shared")
        .collect(Collectors.toList());
  }

  public ScoutSharedIdStructureTest(Class<? extends IId> idClass) {
    super(idClass);
  }
}
