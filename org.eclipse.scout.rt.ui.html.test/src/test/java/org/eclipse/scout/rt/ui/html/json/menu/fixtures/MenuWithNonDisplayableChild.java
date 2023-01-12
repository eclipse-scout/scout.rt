/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
