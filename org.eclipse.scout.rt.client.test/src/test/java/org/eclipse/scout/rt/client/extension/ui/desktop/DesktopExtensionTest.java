/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.desktop;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.service.SERVICES;
import org.junit.Assert;
import org.junit.Test;

public class DesktopExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testExecInitExtension() throws ProcessingException {
    SERVICES.getService(IExtensionRegistry.class).register(DesktopExt01.class, Desktop.class);
    // test code
    new Desktop().initDesktop();
    Assert.assertEquals(1, desktopExecInitCounter.get());
    Assert.assertEquals(1, ext01ExecInitCounter.get());
  }

  private static AtomicInteger desktopExecInitCounter = new AtomicInteger(0);

  private static class Desktop extends AbstractDesktop {

    @Override
    protected void execInit() throws ProcessingException {
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
    public void execInit(DesktopInitChain chain) throws ProcessingException {
      ext01ExecInitCounter.incrementAndGet();
      chain.execInit();
    }
  }
}
