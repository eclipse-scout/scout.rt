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
package org.eclipse.scout.rt.platform.cdi.internal;

import java.net.URL;

/**
 * This class represents a beans.xml. For OSGi support classloading is delegated to {@link IBeansXml#loadClass(String)}.
 *
 * @since 5.1
 */
public interface IBeansXml {

  /**
   * @return the url to the plugin.xml file.
   */
  URL getUrl();

  /**
   * This method is supported for class loading working with our without OSGi. This class can be removed and replaced
   * through {@link Class#forName(String)} for only no-osgi support.
   *
   * @param fullyQuallifiedName
   * @return
   * @throws ClassNotFoundException
   */
  Class<?> loadClass(String fullyQuallifiedName) throws ClassNotFoundException;

}
