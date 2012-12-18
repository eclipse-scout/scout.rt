/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench;

import org.eclipse.rap.rwt.application.IEntryPoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * <h3>AbstractWorkbenchEntryPoint</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.03.2011
 */
public abstract class AbstractWorkbenchEntryPoint implements IEntryPoint {

  @Override
  public int createUI() {
    Display display = PlatformUI.createDisplay();
    int result = PlatformUI.createAndRunWorkbench(display, createWorkbenchAdvisor());
    display.dispose();
    return result;
  }

  protected abstract WorkbenchAdvisor createWorkbenchAdvisor();

}
