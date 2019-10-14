/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.menu.fixtures;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("85d47445-cb3c-4a8b-ac9b-3358c8301769")
public class MenuWithNonDisplayableChild extends AbstractMenu {

  @Order(10)
  @ClassId("ce0f8211-9b4f-4266-a1c6-fd2976bb5803")
  public class DisplayableMenu extends AbstractMenu {
  }

  @Order(20)
  @ClassId("5aafebdb-43d2-4687-8c23-7ed4271cd0ef")
  public class NonDisplayableMenu extends AbstractMenu {

    @Override
    protected void execInitAction() {
      setVisibleGranted(false);
    }
  }
}
