/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action.menu.checkbox;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.AbstractCheckBoxMenu;

public abstract class AbstractCheckBoxMenuExtension<OWNER extends AbstractCheckBoxMenu> extends AbstractMenuExtension<OWNER> implements ICheckBoxMenuExtension<OWNER> {

  public AbstractCheckBoxMenuExtension(OWNER owner) {
    super(owner);
  }
}
