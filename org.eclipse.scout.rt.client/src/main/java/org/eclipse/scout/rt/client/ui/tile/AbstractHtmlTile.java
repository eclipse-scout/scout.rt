/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.res.AttachmentSupport;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

@ClassId("22f26922-32e7-49f1-9180-b48fbb5e75cd")
public abstract class AbstractHtmlTile extends AbstractTile implements IHtmlTile {

  private AttachmentSupport m_attachmentSupport;

  public AbstractHtmlTile() {
    this(true);
    m_attachmentSupport = BEANS.get(AttachmentSupport.class);
  }

  public AbstractHtmlTile(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setContent(getConfiguredContent());
    setHtmlEnabled(true);
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredContent() {
    return null;
  }

  @Override
  public String getContent() {
    return propertySupport.getPropertyString(PROP_CONTENT);
  }

  @Override
  public void setContent(String content) {
    propertySupport.setProperty(PROP_CONTENT, content);
  }

  @Override
  public void setAttachments(Collection<? extends BinaryResource> attachments) {
    m_attachmentSupport.setAttachments(attachments);
  }

  @Override
  public void addAttachment(BinaryResource resource) {
    m_attachmentSupport.addAttachment(resource);
  }

  @Override
  public void removeAttachment(BinaryResource resource) {
    m_attachmentSupport.removeAttachment(resource);
  }

  @Override
  public Set<BinaryResource> getAttachments() {
    return m_attachmentSupport.getAttachments();
  }

  @Override
  public BinaryResource getAttachment(String filename) {
    return m_attachmentSupport.getAttachment(filename);
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_HTML_ENABLED, enabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

}
