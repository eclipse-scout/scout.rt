/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * A combo menu combines two or more child action, but renders them top level as a single menu. This can be used to
 * combine a simple menu which executes an action, and another menu which has child actions.
 */
@ClassId("0acca6e4-bdad-40e2-af16-2e3907ae6450")
public abstract class AbstractComboMenu extends AbstractMenu implements IComboMenu {

  public AbstractComboMenu() {
    this(true);
  }

  public AbstractComboMenu(boolean callInitializer) {
    super(callInitializer);
  }
}
