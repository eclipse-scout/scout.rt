/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.inventory.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class JandexInventoryBuilderTest {

  @Test
  public void testScanModuleWithSpaceInPath() throws IOException {
    JandexInventoryBuilder builder = new JandexInventoryBuilder();
    builder.scanModule(getClass().getResource("test repository/META-INF/scout.xml"));
    builder.scanModule(new URL("jar:" + getClass().getResource("test repository/test.jar_").toExternalForm() + "!/META-INF/scout.xml"));
    assertEquals(2, builder.getIndexList().size());
  }
}
