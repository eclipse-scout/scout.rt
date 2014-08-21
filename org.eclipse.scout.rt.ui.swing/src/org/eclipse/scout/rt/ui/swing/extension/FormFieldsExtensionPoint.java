/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.extension;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.extension.internal.FormFieldExtension;

public final class FormFieldsExtensionPoint {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormFieldsExtensionPoint.class);

  private FormFieldsExtensionPoint() {
  }

  public static IFormFieldExtension[] getFormFieldExtensions() {
    ArrayList<IFormFieldExtension> formFieldExtensionList = new ArrayList<IFormFieldExtension>();
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(Activator.PLUGIN_ID, "formfields");
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        String name = element.getAttribute("name");
        boolean active = "true".equalsIgnoreCase(element.getAttribute("active"));
        FormFieldExtension formFieldExt = new FormFieldExtension(name);
        formFieldExt.setContibuterBundleId(extension.getContributor().getName());
        formFieldExt.setActive(active);
        formFieldExt.setScope(getScopePriority(element.getAttribute("scope")));
        formFieldExt.setModelClassName(element.getAttribute("modelClass"));
        formFieldExt.setFactoryClassName(getClassName(element.getChildren("factory"), "class"));
        formFieldExt.setUiClassName(getClassName(element.getChildren("uiClass"), "class"));
        formFieldExtensionList.add(formFieldExt);
      }
    }
    return formFieldExtensionList.toArray(new IFormFieldExtension[formFieldExtensionList.size()]);
  }

  private static String getClassName(IConfigurationElement[] elements, String attribute) {
    String clazzName = null;
    if (elements != null && elements.length == 1) {
      clazzName = elements[0].getAttribute(attribute);

    }
    return clazzName;
  }

  private static int getScopePriority(String scope) {
    int prio = IFormFieldExtension.SCOPE_DEFAULT;
    if (StringUtility.isNullOrEmpty(scope) || "default".equalsIgnoreCase(scope)) {
      prio = IFormFieldExtension.SCOPE_DEFAULT;
    }
    else if ("global".equalsIgnoreCase(scope)) {
      prio = IFormFieldExtension.SCOPE_GLOBAL;
    }
    return prio;
  }

}
