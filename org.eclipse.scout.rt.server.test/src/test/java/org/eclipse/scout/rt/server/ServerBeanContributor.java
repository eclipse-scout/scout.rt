package org.eclipse.scout.rt.server;

import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;

public class ServerBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IClassInventory classInventory, IBeanContext context) {
    context.registerClass(TestServerSession.class);
  }
}
