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
package org.eclipse.scout.rt.client;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.context.RunMonitor;

public class ForceGCJob implements IRunnable {
  @Override
  public void run() throws Exception {
    int counter = 0;
    Thread.sleep(10000);
    while (counter < 7 && !Thread.interrupted() && !RunMonitor.CURRENT.get().isCancelled() && (Runtime.getRuntime().freeMemory() > 30000000 || counter < 2)) {
      System.gc();
      counter++;
      Thread.sleep(10000);
    }
  }
}
