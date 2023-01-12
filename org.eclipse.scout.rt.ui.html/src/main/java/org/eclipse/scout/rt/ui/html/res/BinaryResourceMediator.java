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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonBean;

/**
 * {@link BinaryResourceMediator} can be used by any {@link IBinaryResourceProvider} in order to provide binary
 * resources defined in any child (such as {@link JsonBean} of the {@link IJsonAdapter}.
 * <p>
 * The child must add the binary resource by calling {@link BinaryResourceMediator#addBinaryResource(BinaryResource)}.
 * <p>
 * The implementation of the {@link IBinaryResourceProvider} can call
 * {@link BinaryResourceMediator#getBinaryResourceHolder(String)} to retrieve the BinearyResourceHolder.
 */
public class BinaryResourceMediator {

  private final IJsonAdapter<?> m_jsonAdapter;
  private final Map<String, BinaryResource> m_binaryResources = new HashMap<>(0);

  public BinaryResourceMediator(IJsonAdapter<?> jsonAdapter) {
    m_jsonAdapter = jsonAdapter;
  }

  public void addBinaryResource(BinaryResource binaryResource) {
    m_binaryResources.put(BinaryResourceUrlUtility.getFilenameWithFingerprint(binaryResource), binaryResource);
  }

  public String createUrl(BinaryResource binaryResource) {
    return BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(m_jsonAdapter, binaryResource);
  }

  public BinaryResourceHolder getBinaryResourceHolder(String filenameWithFingerprint) {
    BinaryResource binaryResource = m_binaryResources.get(filenameWithFingerprint);
    if (binaryResource != null) {
      return new BinaryResourceHolder(binaryResource);
    }
    return null;
  }
}
