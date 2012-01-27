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
package org.eclipse.scout.http.servletfilter.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public final class ServletFilterExtensionPoint {
  private static Object filtersLock = new Object();
  private static List<FilterConfigImpl> filters;

  private ServletFilterExtensionPoint() {
  }

  public static List<FilterConfigImpl> getExtensions() {
    if (filters == null) {
      synchronized (filtersLock) {
        TreeMap<Double, FilterConfigImpl> tempMap = new TreeMap<Double, FilterConfigImpl>();
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        // add change listener
        reg.addListener(new IRegistryEventListener() {
          @Override
          public void added(IExtension[] extensions) {
            notifyExtensionPointChanged();
          }

          @Override
          public void removed(IExtension[] extensions) {
            notifyExtensionPointChanged();
          }

          @Override
          public void added(IExtensionPoint[] extensionPoints) {
            notifyExtensionPointChanged();
          }

          @Override
          public void removed(IExtensionPoint[] extensionPoints) {
            notifyExtensionPointChanged();
          }
        }, Activator.PLUGIN_ID + ".filters");
        IExtensionPoint xp = reg.getExtensionPoint(Activator.PLUGIN_ID, "filters");
        IExtension[] extensions = xp.getExtensions();
        double index = 0;
        for (IExtension extension : extensions) {
          Bundle contributorBundle = Platform.getBundle(extension.getContributor().getName());
          IConfigurationElement[] serviceElements = extension.getConfigurationElements();
          for (IConfigurationElement serviceElement : serviceElements) {
            String className = serviceElement.getAttribute("class");
            String runOrderText = serviceElement.getAttribute("ranking");
            try {
              double runOrder = 0.0;
              if (runOrderText != null && runOrderText.length() > 0) {
                runOrder = Double.parseDouble(runOrderText);
              }
              for (String alias : serviceElement.getAttribute("aliases").split("[ ,;]")) {
                alias = alias.trim();
                if (alias.length() > 0) {
                  FilterConfigImpl ref = new FilterConfigImpl(alias, serviceElement, contributorBundle);
                  tempMap.put(runOrder + index * (1e-6), ref);
                  index++;
                }
              }
            }
            catch (Throwable t) {
              Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, "create filter: " + className, t));
            }
          }
        }
        filters = new ArrayList<FilterConfigImpl>(tempMap.values());
      }
    }
    return filters;
  }

  private static void notifyExtensionPointChanged() {
    System.out.println("#ExtensionPointChanged(filters)");
    synchronized (filtersLock) {
      if (filters != null) {
        for (FilterConfigImpl ref : filters) {
          ref.destroy();
        }
      }
      filters = null;
    }
  }

}
