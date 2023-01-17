/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.dnd;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class ResourceListTransferObject extends TransferObject {
  private final List<BinaryResource> m_resources;

  public ResourceListTransferObject(BinaryResource... resources) {
    this(Arrays.asList(resources));
  }

  public ResourceListTransferObject(List<BinaryResource> resources) {
    m_resources = CollectionUtility.arrayListWithoutNullElements(resources);
  }

  public List<BinaryResource> getResources() {
    return CollectionUtility.arrayList(m_resources);
  }

  @Override
  public String toString() {
    return ResourceListTransferObject.class.getSimpleName() + "[resources=" + CollectionUtility.format(m_resources) + "]";
  }
}
