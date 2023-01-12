/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
