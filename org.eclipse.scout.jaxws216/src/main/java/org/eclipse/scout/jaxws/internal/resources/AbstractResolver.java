/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;

abstract class AbstractResolver<T> {

  private static final Logger LOG = Logger.getLogger("com.sun.xml.ws.server.http");

  private Bundle[] m_bundles;

  public AbstractResolver(Bundle... bundles) {
    m_bundles = bundles;
  }

  public void setBundles(Bundle[] bundles) {
    m_bundles = bundles;
  }

  public T resolve() {
    for (Bundle bundle : m_bundles) {
      // bundle must be in state RESOLVED at minimum
      if (bundle.getState() < Bundle.RESOLVED) {
        LOG.log(Level.SEVERE, "JAXWS resources could not be loaded as bundle '" + bundle.getSymbolicName() + "' is not in state RESOLVED.");
        continue;
      }

      try {
        return resolve(bundle);
      }
      catch (ResourceNotFoundException e) {
        //nop
      }
    }
    return resolveEmpty();
  }

  public abstract T resolve(Bundle bundle) throws ResourceNotFoundException;

  public T resolveEmpty() {
    return null;
  }

  /**
   * Exception to indicate that no resource is found.
   */
  static class ResourceNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
  }
}
