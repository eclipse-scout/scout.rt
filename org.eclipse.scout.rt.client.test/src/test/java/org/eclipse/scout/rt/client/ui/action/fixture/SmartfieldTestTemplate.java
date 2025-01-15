/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.fixture;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

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
