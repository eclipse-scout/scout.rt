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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.eclipse.core.runtime.Status;
import org.eclipse.scout.net.NetActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class InternalEclipseAuthenticator extends Authenticator {

  public InternalEclipseAuthenticator() {
  }

  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    PasswordAuthentication result = null;
    Authenticator auth = new EclipseAuthenticatorLocator().locate();
    if (auth == null) {
      // use osgi service
      BundleContext context = NetActivator.getDefault().getBundle().getBundleContext();
      ServiceReference ref = context.getServiceReference(Authenticator.class.getName());
      if (ref != null) {
        auth = (Authenticator) context.getService(ref);
        context.ungetService(ref);
      }
    }
    if (auth != null) {
      try {
        result = reflectPasswordAuthentication(auth);
      }
      catch (Throwable ex) {
        NetActivator.getDefault().getLog().log(new Status(Status.ERROR, NetActivator.PLUGIN_ID, getRequestingURL() + " " + getRequestorType(), ex));
        throw new SecurityException(ex);
      }
    }
    if (result != null) {
      if (NetActivator.DEBUG) {
        NetActivator.getDefault().getLog().log(new Status(Status.INFO, NetActivator.PLUGIN_ID, "net.auth SUCCESS " + getRequestingURL() + " " + getRequestorType() + " " + result.getUserName()));
      }
      return result;
    }
    else {
      if (NetActivator.DEBUG) {
        NetActivator.getDefault().getLog().log(new Status(Status.INFO, NetActivator.PLUGIN_ID, "net.auth NONE " + getRequestingURL() + " " + getRequestorType()));
      }
      return null;
    }
  }

  private PasswordAuthentication reflectPasswordAuthentication(Authenticator delegate) throws Throwable {
    reflectCopyField(this, delegate, "requestingHost");
    reflectCopyField(this, delegate, "requestingSite");
    reflectCopyField(this, delegate, "requestingPort");
    reflectCopyField(this, delegate, "requestingProtocol");
    reflectCopyField(this, delegate, "requestingPrompt");
    reflectCopyField(this, delegate, "requestingScheme");
    reflectCopyField(this, delegate, "requestingURL");
    reflectCopyField(this, delegate, "requestingAuthType");
    //
    Method m = delegate.getClass().getDeclaredMethod("getPasswordAuthentication");
    m.setAccessible(true);
    return (PasswordAuthentication) m.invoke(delegate);
  }

  private void reflectCopyField(Authenticator from, Authenticator to, String fieldName) throws Throwable {
    Field f = Authenticator.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    Object value = f.get(from);
    f.set(to, value);
  }

}
