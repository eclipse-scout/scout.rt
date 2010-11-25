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
package org.eclipse.scout.rt.testing.shared;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class ScoutJUnitPluginTestApplication implements IApplication {

  public Object start(IApplicationContext context) throws Exception {
    ScoutJUnitPluginTestExecutor scoutJUnitPluginTestExecutor = new ScoutJUnitPluginTestExecutor();
    int exitCode = scoutJUnitPluginTestExecutor.runAllTests();
    return exitCode;
  }

  public void stop() {
  }

}
