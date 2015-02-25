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
package org.eclipse.scout.net.internal;

import java.net.Authenticator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.net.NetActivator;

/**
 * locate authenticator at extension point "org.eclipse.core.net.authenticator"
 */
public class EclipseAuthenticatorLocator {

  public Authenticator locate() {
    IExtensionRegistry xreg = Platform.getExtensionRegistry();
    if (xreg != null) {
      IExtensionPoint xp = xreg.getExtensionPoint("org.eclipse.core.net", "authenticator");
      IExtension[] extensions = xp.getExtensions();
      for (IExtension extension : extensions) {
        for (IConfigurationElement authenticatorElement : extension.getConfigurationElements()) {
          try {
            if (authenticatorElement.getName().equals("authenticator")) {
              return (Authenticator) authenticatorElement.createExecutableExtension("class");
            }
          }
          catch (CoreException e) {
            NetActivator.getDefault().getLog().log(new Status(Status.ERROR, NetActivator.PLUGIN_ID, "authenticator: " + authenticatorElement.getAttribute("class"), e));
          }
        }
      }
    }
    return null;
  }

}
