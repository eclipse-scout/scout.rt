/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.net.tests;

import java.net.URL;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.net.NetActivator;

public class TestApplication implements IApplication {

  public Object start(IApplicationContext context) throws Exception {
    System.out.println("Hello RCP World!");

    NetActivator.install();

    URL url = new URL("http://www.google.ch");
    for (int i = 0; i < 10; i++) {
      long t = System.nanoTime();
      url.openConnection().getInputStream();
      long dt = System.nanoTime() - t;
      System.out.println("Accessing " + url + " took " + (dt / 1000000L) + " ms");
    }

    return EXIT_OK;
  }

  public void stop() {
  }
}
