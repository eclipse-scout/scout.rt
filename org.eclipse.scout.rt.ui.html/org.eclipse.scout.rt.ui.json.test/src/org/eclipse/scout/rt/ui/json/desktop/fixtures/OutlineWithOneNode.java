package org.eclipse.scout.rt.ui.json.desktop.fixtures;

import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public class OutlineWithOneNode extends AbstractOutline {

  @Override
  protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
    NodePage nodePage = new NodePage();
    pageList.add(nodePage);
  }

}
