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
package org.eclipse.scout.rt.client.ui.action.fixture;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;

/**
 * A test smartfield with a dummy classId
 */
public abstract class SmartfieldTestTemplate extends AbstractSmartField<Long> {
  static final String TEST_MENU_ID = "TEST_MENU_ID";

  @Order(10)
  @ClassId(TEST_MENU_ID)
  public class TestMenu extends AbstractMenu {
  }

}
