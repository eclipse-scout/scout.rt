/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeInitTreeChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.AbstractOutlineExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.platform.BEANS;

public class OutlineExtension extends AbstractOutlineExtension<AbstractOutline> {

  public OutlineExtension(AbstractOutline owner) {
    super(owner);
  }

  @Override
  public void execInitTree(TreeInitTreeChain chain) {
    super.execInitTree(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformOutline(getOwner());
  }

}
