/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DesktopExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExecInitExtension() {
    BEANS.get(IExtensionRegistry.class).register(DesktopExt01.class, Desktop.class);
    // test code
    new Desktop().init();
    Assert.assertEquals(1, desktopExecInitCounter.get());
    Assert.assertEquals(1, ext01ExecInitCounter.get());
  }

  private static AtomicInteger desktopExecInitCounter = new AtomicInteger(0);

  private static class Desktop extends AbstractDesktop {

    @Override
    protected void execInit() {
      desktopExecInitCounter.incrementAndGet();
      super.execInit();
    }
  }

  private static AtomicInteger ext01ExecInitCounter = new AtomicInteger(0);

  protected static class DesktopExt01 extends AbstractDesktopExtension<Desktop> {

    public DesktopExt01(Desktop owner) {
      super(owner);
    }

    @Override
    public void execInit(DesktopInitChain chain) {
      ext01ExecInitCounter.incrementAndGet();
      chain.execInit();
    }
  }
}
