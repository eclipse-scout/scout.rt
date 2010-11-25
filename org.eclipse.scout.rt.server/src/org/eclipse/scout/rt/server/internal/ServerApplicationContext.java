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

import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;

public class ServerApplicationContext implements IApplicationContext {

  public void applicationRunning() {
  }

  public Map getArguments() {
    return null;
  }

  public String getBrandingApplication() {
    return null;
  }

  public Bundle getBrandingBundle() {
    return null;
  }

  public String getBrandingDescription() {
    return null;
  }

  public String getBrandingId() {
    return null;
  }

  public String getBrandingName() {
    return null;
  }

  public String getBrandingProperty(String key) {
    return null;
  }

}
