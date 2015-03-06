package org.eclipse.scout.rt.server;

import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.cdi.ServerBeanInstanceFactory;
import org.eclipse.scout.rt.server.job.internal.ServerJobManager;

public class ServerBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IBeanContext context) {
    context.registerClass(ProcessInspector.class);
    context.registerClass(ServerJobManager.class);
    context.registerClass(ServerBeanInstanceFactory.class);
  }

}
