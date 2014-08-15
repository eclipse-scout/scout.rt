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
package org.eclipse.scout.rt.client.ui.form;

import java.util.List;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;

public class FormContextMenu extends AbstractContextMenu implements IFormContextMenu {

  public FormContextMenu(IPropertyObserver owner, List<? extends IMenu> initialChildList) {
    super(owner, initialChildList);
  }

  @Override
  public IForm getOwner() {
    return (IForm) super.getOwner();
  }
}
