/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   * {@link #addHttpResponseInterceptor(IHttpResponseInterceptor)} and
   * {@link #removeHttpResponseInterceptor(IHttpResponseInterceptor)} methods). Never <code>null</code>.
   */
  public Set<IHttpResponseInterceptor> getHttpResponseInterceptors() {
    return m_httpResponseInterceptors;
  }
}
