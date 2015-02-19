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

import org.eclipse.scout.rt.client.services.ProxyXmlVisitor;
import org.eclipse.scout.rt.client.ui.desktop.DesktopExtensionPluginXmlVisitor;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.pluginxml.internal.PluginXmlParser;

/**
 *
 */
public class ClientBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IBeanContext context) {
    PluginXmlParser.get().visit(new DesktopExtensionPluginXmlVisitor(context));
    PluginXmlParser.get().visit(new ProxyXmlVisitor(context));
  }

}
