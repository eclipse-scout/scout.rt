package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public class NonDisplayableOutlineWithOneNode extends AbstractOutline {

  @Override
  protected void execCreateChildPages(List<IPage<?>> pageList) {
    NodePage nodePage = new NodePage();
    pageList.add(nodePage);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setVisibleGranted(false);
  }

}
