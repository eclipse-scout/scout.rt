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
package org.eclipse.scout.rt.client.extension.ui.outline.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.outline.desktop.fixture.ExtensionTestDesktop;
import org.eclipse.scout.rt.client.extension.ui.outline.desktop.fixture.FirstOutline;
import org.eclipse.scout.rt.client.extension.ui.outline.desktop.fixture.SecondOutline;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DesktopMoveOutlinesTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() {
    ExtensionTestDesktop desktop = new ExtensionTestDesktop();
    assertOutlines(desktop, FirstOutline.class, SecondOutline.class);
  }

  @Test
  public void testMoveOutline() {
    BEANS.get(IExtensionRegistry.class).registerMove(FirstOutline.class, 30);

    ExtensionTestDesktop desktop = new ExtensionTestDesktop();
    assertOutlines(desktop, SecondOutline.class, FirstOutline.class);
    assertEquals(30, desktop.getAvailableOutlines().get(1).getOrder(), 0);
  }

  @Test
  public void testMoveOutlineMultipleTimes() {
    BEANS.get(IExtensionRegistry.class).registerMove(FirstOutline.class, 30);
    BEANS.get(IExtensionRegistry.class).registerMove(FirstOutline.class, 50);

    ExtensionTestDesktop desktop = new ExtensionTestDesktop();
    assertOutlines(desktop, SecondOutline.class, FirstOutline.class);
    assertEquals(50, desktop.getAvailableOutlines().get(1).getOrder(), 0);
  }

  protected static void assertOutlines(ExtensionTestDesktop desktop, Class<?>... outlineClasses) {
    List<IOutline> outlines = desktop.getAvailableOutlines();
    assertEquals(outlineClasses.length, outlines.size());
    for (int i = 0; i < outlineClasses.length; i++) {
      assertTrue(outlineClasses[i] == outlines.get(i).getClass());
    }
  }
}
