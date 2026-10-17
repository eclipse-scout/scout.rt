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

@ClassId("7f52e6fe-b61c-415a-9a2b-180674ba181b")
public abstract class AbstractMoveTableRowUpMenu extends AbstractMoveTableRowMenu {

  @Override
  protected boolean getConfiguredMoveUp() {
    return true;
  }
}
