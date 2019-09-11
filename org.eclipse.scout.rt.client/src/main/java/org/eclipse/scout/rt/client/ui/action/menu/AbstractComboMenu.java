/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
