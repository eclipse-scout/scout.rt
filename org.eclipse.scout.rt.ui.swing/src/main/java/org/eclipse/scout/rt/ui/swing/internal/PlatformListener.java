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
package org.eclipse.scout.rt.ui.swing.internal;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.platform.pluginxml.internal.PluginXmlParser;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldExtensions;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldsPluginXmlVisitor;

public class PlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanContextPrepared) {
      IBeanContext context = event.getSource().getBeanContext();
      FormFieldExtensions formFieldExtensions = OBJ.one(FormFieldExtensions.class);

      // register form fields from plugin.xml
      PluginXmlParser.get().visit(new FormFieldsPluginXmlVisitor(context, formFieldExtensions));
    }
  }

}
