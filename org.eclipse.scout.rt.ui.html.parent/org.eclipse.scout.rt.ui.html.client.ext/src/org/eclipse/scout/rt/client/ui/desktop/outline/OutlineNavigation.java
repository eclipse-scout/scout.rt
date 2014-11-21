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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public final class OutlineNavigation {

  public static AbstractOutlineNavigationMenu createUp(IOutline outline) {
    return initMenu(new OutlineNavigateUpMenu(outline));
  }

  public static AbstractOutlineNavigationMenu createDown(IOutline outline) {
    return initMenu(new OutlineNavigateDownMenu(outline));
  }

  private static AbstractOutlineNavigationMenu initMenu(AbstractOutlineNavigationMenu menu) {
    try {
      menu.initAction();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    return menu;
  }

}
