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

import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * Wrapper used by {@link IBinaryResourceProvider} to hold a binary resource and some additional data.
 */
public class BinaryResourceHolder {

  private final BinaryResource m_binaryResource;
  private final boolean m_download;

  public BinaryResourceHolder(BinaryResource binaryResource) {
    this(binaryResource, false);
  }

  public BinaryResourceHolder(BinaryResource binaryResource, boolean download) {
    m_binaryResource = binaryResource;
    m_download = download;
  }

  /**
   * @return the provided binary resource (may be <code>null</code>)
   */
  public BinaryResource get() {
    return m_binaryResource;
  }

  /**
   * @return <code>true</code> if the user requested the resource to be downloaded ("save as" dialog), false otherwise
   *         (e.g. inline use, such as an image for the ImageFiel).
   */
  public boolean isDownload() {
    return m_download;
  }
}
