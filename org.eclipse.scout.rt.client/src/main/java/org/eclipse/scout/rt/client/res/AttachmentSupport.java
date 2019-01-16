/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.res;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@Bean
public class AttachmentSupport implements IAttachmentSupport {

  private Map<String, BinaryResource> m_attachments;

  public AttachmentSupport() {
    m_attachments = new HashMap<>(0);
  }

  @Override
  public Set<BinaryResource> getAttachments() {
    return CollectionUtility.hashSet(m_attachments.values());
  }

  @Override
  public BinaryResource getAttachment(String filename) {
    return m_attachments.get(filename);
  }

  @Override
  public void setAttachments(Collection<? extends BinaryResource> attachments) {
    if (attachments == null) {
      m_attachments = new HashMap<>(0);
      return;
    }
    Map<String, BinaryResource> newMap = new HashMap<>(attachments.size());
    for (BinaryResource attachment : attachments) {
      if (attachment != null) {
        newMap.put(attachment.getFilename(), attachment);
      }
    }
    m_attachments = newMap;
  }

  public void addAttachment(BinaryResource attachment) {
    if (attachment != null) {
      m_attachments.put(attachment.getFilename(), attachment);
    }
  }

  public void removeAttachment(BinaryResource attachment) {
    if (attachment != null) {
      m_attachments.remove(attachment.getFilename());
    }
  }

}
