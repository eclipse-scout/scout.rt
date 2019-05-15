/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.id.IStringId;
import org.eclipse.scout.rt.dataobject.testing.AbstractStringIdStructureTest;
import org.junit.runners.Parameterized.Parameters;

public class StringIdStructureTest extends AbstractStringIdStructureTest {

  @Parameters(name = "{0}")
  public static Iterable<? extends Object> parameters() {
    return streamStringIdClasses("org.eclipse.scout.rt.jackson")
        .collect(Collectors.toList());
  }

  public StringIdStructureTest(Class<? extends IStringId> id) {
    super(id);
  }
}
