/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.menu.fixtures;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;

public class MenuWithNonDisplayableChild extends AbstractMenu {

  @Order(10)
  public class DisplayableMenu extends AbstractMenu {
  }

  @Order(20)
  public class NonDisplayableMenu extends AbstractMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }
}
