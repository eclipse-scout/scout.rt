package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithNodesExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

public class MobilePageWithNodesExtension extends AbstractPageWithNodesExtension<AbstractPageWithNodes> {

  public MobilePageWithNodesExtension(AbstractPageWithNodes owner) {
    super(owner);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    super.execInitPage(chain);
    if (!UserAgentUtility.isMobileDevice()) {
      return;
    }
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformPage(getOwner());
  }

}
