package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.platform.BEANS;

public class PageExtension extends AbstractPageExtension<AbstractPage> {

  public PageExtension(AbstractPage owner) {
    super(owner);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    super.execInitPage(chain);
    IDeviceTransformer deviceTransformer = BEANS.get(IDeviceTransformationService.class).getDeviceTransformer();
    if (deviceTransformer == null) {
      return;
    }
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformPage(getOwner());
  }

}
