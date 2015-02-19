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
 *
 */
public class BeansXml implements IBeansXml {

  private final URL m_beanXmlUrl;

  public BeansXml(URL beanXmlUrl) {
    m_beanXmlUrl = beanXmlUrl;
  }

  @Override
  public URL getUrl() {
    return m_beanXmlUrl;
  }

  @Override
  public Class<?> loadClass(String fullyQuallifiedName) throws ClassNotFoundException {
    return Class.forName(fullyQuallifiedName);
  }

}
