package org.eclipse.scout.rt.server;

import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;

public class ServerBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IBeanContext context) {
    context.registerClass(TestServerSession.class);
  }
}
