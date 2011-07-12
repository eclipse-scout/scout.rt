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
package org.eclipse.scout.rt.server.internal;

import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;

/**
 * Empty Application Context
 */
public class ServerApplicationContext implements IApplicationContext {

  @Override
  public void applicationRunning() {
  }

  @Override
  public Map getArguments() {
    return null;
  }

  @Override
  public String getBrandingApplication() {
    return null;
  }

  @Override
  public Bundle getBrandingBundle() {
    return null;
  }

  @Override
  public String getBrandingDescription() {
    return null;
  }

  @Override
  public String getBrandingId() {
    return null;
  }

  @Override
  public String getBrandingName() {
    return null;
  }

  @Override
  public String getBrandingProperty(String key) {
    return null;
  }

//  @Override In Eclipse 3.4 this method is not in the Interface
  public void setResult(Object result, IApplication application) {
  }

}
