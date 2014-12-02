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
package org.eclipse.scout.rt.ui.html;

import java.net.URL;

import javax.servlet.ServletContext;

/**
 * Locate script and other web resources in either osgi or java jee environments
 */
public interface IWebContentResourceLocator {

  /**
   * @return the source (or template file) of a js or css script out of the folders
   *         <code>anyJarFile!/META-INF/resources/src/main/js</code>
   *         <p>
   *         See {@link ServletContext#getResourcePaths(String)}
   */
  Script getScriptSource(String scriptPath);

  /**
   * @return a resource out of the folders <code>anyJarFile!/META-INF/resources/WebContent</code>
   *         <p>
   *         See {@link ServletContext#getResourcePaths(String)}
   */
  URL getWebContentResource(String resourcePath);
}
