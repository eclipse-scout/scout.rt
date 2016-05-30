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
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * @since 3.9.0
 */
@Order(5300)
public class TabletDeviceTransformer extends AbstractDeviceTransformer {

  @Override
  public boolean isActive() {
    return UserAgentUtility.isTabletDevice();
  }

  @Override
  public void transformDesktop() {
    getDesktop().setCacheSplitterPosition(false);
  }

  @Override
  public void transformOutline(IOutline outline) {
    outline.setDisplayStyle(ITree.DISPLAY_STYLE_BREADCRUMB);
  }

}
