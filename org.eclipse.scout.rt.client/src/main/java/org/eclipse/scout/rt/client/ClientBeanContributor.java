/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.client.cdi.ClientBeanInstanceFactory;
import org.eclipse.scout.rt.client.job.internal.ClientJobManager;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.session.ClientSessionProviderWithCache;
import org.eclipse.scout.rt.client.ui.desktop.DesktopExtensionPluginXmlVisitor;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.pluginxml.internal.PluginXmlParser;

public class ClientBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IBeanContext context) {
    PluginXmlParser.get().visit(new DesktopExtensionPluginXmlVisitor(context));
    context.registerClass(IconLocator.class);
    context.registerClass(ClientJobManager.class);
    context.registerClass(ModelJobManager.class);
    context.registerClass(ClientBeanInstanceFactory.class);
    context.registerClass(ClientSessionProvider.class);
    context.registerClass(ClientSessionProviderWithCache.class);
  }
}
