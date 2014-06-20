package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public class OutlineWithOneNode extends AbstractOutline {

  @Override
  protected void execCreateChildPages(List<IPage> pageList) throws ProcessingException {
    NodePage nodePage = new NodePage();
    pageList.add(nodePage);
  }

}
