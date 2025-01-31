/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
