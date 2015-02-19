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
package org.eclipse.scout.rt.ui.html.services;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.rt.ui.html.Activator;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;

/**
 *
 */
public class WebContentResourceLocator extends AbstractService implements IWebContentService {

  private Bundle m_bundle;

  @Override
  protected void initializeService() {
    super.initializeService();
    m_bundle = Platform.getBundle(Activator.PLUGIN_ID);
  }

  @Override
  public URL getResource(String resourcePath) {
    return m_bundle.getEntry(resourcePath);
  }

}
