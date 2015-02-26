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
package org.eclipse.scout.service.internal;

import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.pluginxml.internal.PluginXmlParser;
import org.eclipse.scout.service.DefaultServiceInitializer;

/**
 *
 */
public class ServiceBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IBeanContext context) {
    // register default inistializer
    context.registerClass(DefaultServiceInitializer.class);

    // parse all servies registered as extensions in plugin.xml.
    PluginXmlParser.get().visit(new ServiceXmlVisitor(context));
  }

}
