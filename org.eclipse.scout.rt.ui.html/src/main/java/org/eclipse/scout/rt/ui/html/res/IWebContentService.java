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
package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Locate script and other web resources on the classpath
 */
public interface IWebContentService extends IService {

  /**
   * @return the source (or template file) of a js or css script.
   *         <p>
   *         At runtime the prefix / is used, at development time the prefix /src/main/js/ and /src/main/js/ is used.
   */
  URL getScriptSource(String path);

  /**
   * @return a web resource
   *         <p>
   *         At runtime the prefix /WebContent/ is used, at development time the prefix /src/main/resources/WebContent/
   *         and /src/test/resources/WebContent/ is used.
   */
  URL getWebContentResource(String path);
}
