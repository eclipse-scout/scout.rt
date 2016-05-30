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
