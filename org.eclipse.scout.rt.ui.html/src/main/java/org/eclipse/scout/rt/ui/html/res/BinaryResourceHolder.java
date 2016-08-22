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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;

/**
 * Wrapper used by {@link IBinaryResourceProvider} to hold a binary resource and some additional data.
 */
public class BinaryResourceHolder {

  private final BinaryResource m_binaryResource;
  private final Set<IHttpResponseInterceptor> m_httpResponseInterceptors = new HashSet<>();

  public BinaryResourceHolder(BinaryResource binaryResource) {
    m_binaryResource = binaryResource;
  }

  /**
   * @return the provided binary resource (may be <code>null</code>)
   */
  public BinaryResource get() {
    return m_binaryResource;
  }

  public void addHttpResponseInterceptor(IHttpResponseInterceptor interceptor) {
    m_httpResponseInterceptors.add(interceptor);
  }

  public void removeHttpResponseInterceptor(IHttpResponseInterceptor interceptor) {
    m_httpResponseInterceptors.remove(interceptor);
  }

  /**
   * @return live set of associated {@link IHttpResponseInterceptor} (although it is recommended to use the
   *         {@link #addHttpResponseInterceptor(IHttpResponseInterceptor)} and
   *         {@link #removeHttpResponseInterceptor(IHttpResponseInterceptor)} methods). Never <code>null</code>.
   */
  public Set<IHttpResponseInterceptor> getHttpResponseInterceptors() {
    return m_httpResponseInterceptors;
  }
}
