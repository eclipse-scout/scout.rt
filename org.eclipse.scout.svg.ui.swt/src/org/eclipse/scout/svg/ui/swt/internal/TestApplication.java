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
package org.eclipse.scout.svg.ui.swt.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class TestApplication implements IApplication {

  @Override
  public Object start(IApplicationContext context) throws Exception {
    SwtViewer.show();
    Thread.sleep(Long.MAX_VALUE);
    return EXIT_OK;
  }

  @Override
  public void stop() {
  }

}
