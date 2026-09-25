/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("22a7b8d4-422c-4cb0-a5d9-f36bfd81530e")
public abstract class AbstractMoveTableRowDownMenu extends AbstractMoveTableRowMenu {

  @Override
  protected boolean getConfiguredMoveUp() {
    return false;
  }
}
