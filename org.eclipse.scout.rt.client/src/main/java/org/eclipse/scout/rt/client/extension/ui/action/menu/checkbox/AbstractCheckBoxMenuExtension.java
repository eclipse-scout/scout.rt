/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.action.menu.checkbox;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.AbstractCheckBoxMenu;

public abstract class AbstractCheckBoxMenuExtension<OWNER extends AbstractCheckBoxMenu> extends AbstractMenuExtension<OWNER> implements ICheckBoxMenuExtension<OWNER> {

  public AbstractCheckBoxMenuExtension(OWNER owner) {
    super(owner);
  }
}
