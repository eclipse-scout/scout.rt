/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader;

import java.net.URL;

public class JarLocator {
  private final Class<?> m_loaderClass;

  public JarLocator(Class<?> loaderClass) {
    m_loaderClass = loaderClass;
  }

  public URL getResource(String resourceName) {
    URL url = null;
    //path in binary build
    url = m_loaderClass.getResource("/" + resourceName);
    if (url != null) {
      return url;
    }
    //path in development workspace
    url = m_loaderClass.getResource("/src/main/resources/" + resourceName);
    if (url != null) {
      return url;
    }
    url = m_loaderClass.getResource("/target/classes/" + resourceName);
    if (url != null) {
      return url;
    }
    return null;
  }
}
