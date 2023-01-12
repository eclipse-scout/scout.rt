/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action.menu.root;

import org.eclipse.scout.rt.client.extension.ui.action.menu.AbstractMenuExtension;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;

public abstract class AbstractContextMenuExtension<OWNER extends AbstractContextMenu> extends AbstractMenuExtension<OWNER> implements IContextMenuExtension<OWNER> {

  public AbstractContextMenuExtension(OWNER owner) {
    super(owner);
  }
}
