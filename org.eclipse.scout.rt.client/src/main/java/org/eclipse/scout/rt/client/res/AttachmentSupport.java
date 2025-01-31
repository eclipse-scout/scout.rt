/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.res;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.BooleanUtility;

@Bean
public class AttachmentSupport {

  private Map<String, Attachment> m_attachments;

  public AttachmentSupport() {
    m_attachments = new HashMap<>(0);
  }

  public Set<BinaryResource> getAttachments() {
    return getAttachments(null, null);
  }

  public Set<BinaryResource> getAttachments(Boolean uploaded, Boolean referenced) {
    return m_attachments.values().stream()
        .filter(attachment -> (uploaded == null || BooleanUtility.nvl(uploaded) == attachment.isUploaded()) &&
            (referenced == null || BooleanUtility.nvl(referenced) == attachment.isReferenced()))
        .map(Attachment::getBinaryResource)
        .collect(Collectors.toSet());
  }

  public BinaryResource getAttachment(String filename) {
    Attachment attachment = m_attachments.get(filename);
    if (attachment == null) {
      return null;
    }
    return attachment.getBinaryResource();
  }

  public void setAttachments(Collection<? extends BinaryResource> attachments) {
    if (attachments == null) {
      m_attachments = new HashMap<>(0);
      return;
    }
    Map<String, Attachment> newMap = new HashMap<>(attachments.size());
    for (BinaryResource attachment : attachments) {
      if (attachment != null) {
        newMap.put(attachment.getFilename(), new Attachment(attachment));
      }
    }
    m_attachments = newMap;
  }

  public void addAttachment(BinaryResource attachment) {
    addAttachment(attachment, false);
  }

  public void addAttachment(BinaryResource attachment, boolean uploaded) {
    if (attachment != null) {
      m_attachments.put(attachment.getFilename(), new Attachment(attachment, uploaded));
    }
  }

  public void removeAttachment(BinaryResource attachment) {
    if (attachment != null) {
      m_attachments.remove(attachment.getFilename());
    }
  }

  public void setUploaded(BinaryResource attachment0, boolean uploaded) {
    Attachment attachment = m_attachments.get(attachment0.getFilename());
    if (attachment != null) {
      attachment.setUploaded(uploaded);
    }
  }

  public boolean isUploaded(BinaryResource attachment0) {
    Attachment attachment = m_attachments.get(attachment0.getFilename());
    if (attachment == null) {
      return false;
    }
    return attachment.isUploaded();
  }

  public void setReferenced(String filename, boolean referenced) {
    Attachment attachment = m_attachments.get(filename);
    if (attachment != null) {
      attachment.setReferenced(referenced);
    }
  }

  public void setReferenced(BinaryResource attachment, boolean referenced) {
    setReferenced(attachment.getFilename(), referenced);
  }

  public boolean isReferenced(BinaryResource attachment0) {
    Attachment attachment = m_attachments.get(attachment0.getFilename());
    if (attachment == null) {
      return false;
    }
    return attachment.isReferenced();
  }

  /**
   * Deletes all attachments with status !referenced.
   */
  public void cleanup() {
    Set<String> toDelete = m_attachments.values().stream()
        .filter(attachment -> !attachment.isReferenced())
        .map(attachment -> attachment.getBinaryResource().getFilename())
        .collect(Collectors.toSet());
    toDelete.forEach(filename -> m_attachments.remove(filename));
  }
}
