package org.eclipse.scout.rt.testing.server;

import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.testenvironment.AllAccessControlService;

public class ServerBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IClassInventory classInventory, IBeanContext context) {
    context.registerClass(TestServerSession.class);
    context.registerClass(AllAccessControlService.class);
  }
}
