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
package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.commons.resource.BinaryResource;

public class DownloadHandler implements IDownloadHandler {

  private final BinaryResource m_resource;
  private final long m_ttl;

  public DownloadHandler(BinaryResource resource, long ttl) {
    if (resource == null) {
      throw new IllegalArgumentException("resource cannot be null");
    }
    if (ttl <= 0) {
      throw new IllegalArgumentException("TTL must be > 0");
    }
    m_resource = resource;
    m_ttl = ttl;
  }

  @Override
  public long getTTL() {
    return m_ttl;
  }

  @Override
  public BinaryResource getResource() {
    return m_resource;
  }

}
