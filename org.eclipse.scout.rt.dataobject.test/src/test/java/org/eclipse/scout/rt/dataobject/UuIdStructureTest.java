/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject;

import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.eclipse.scout.rt.dataobject.testing.AbstractUuIdStructureTest;
import org.junit.runners.Parameterized.Parameters;

public class UuIdStructureTest extends AbstractUuIdStructureTest {

  @Parameters(name = "{0}")
  public static Iterable<? extends Object> parameters() {
    return streamUuIdClasses("org.eclipse.scout.rt.dataobject")
        .filter(c -> c.getDeclaringClass() != IdFactoryTest.class)
        .collect(Collectors.toList());
  }

  public UuIdStructureTest(Class<? extends IUuId> id) {
    super(id);
  }
}
