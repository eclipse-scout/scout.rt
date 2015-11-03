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
package org.eclipse.scout.rt.ui.html.res.loader;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.ui.html.res.ResourceRequestHandler;

/**
 * Beans of this type are used in the {@link ResourceRequestHandler}
 */
@Bean
public interface IResourceLoaderFactory {

  /**
   * @param req
   * @param resourcePath
   * @return a {@link IResourceLoader} or null if this factory is not handling the request
   */
  IResourceLoader createResourceLoader(HttpServletRequest req, String resourcePath);
}
