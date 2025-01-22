/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons;

import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.testing.AbstractIdStructureTest;
import org.junit.runners.Parameterized.Parameters;

public class ScoutServerCommonsIdStructureTest extends AbstractIdStructureTest {

  @Parameters(name = "{0}")
  public static Iterable<?> parameters() {
    return streamIdClasses("org.eclipse.scout.rt.server.commons")
        .collect(Collectors.toList());
  }

  public ScoutServerCommonsIdStructureTest(Class<? extends IId> idClass) {
    super(idClass);
  }
}
