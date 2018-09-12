/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@ClassId("22f26922-32e7-49f1-9180-b48fbb5e75cd")
public abstract class AbstractHtmlTile extends AbstractTile implements IHtmlTile {
  private Map<String, BinaryResource> m_attachments;

  @Override
  protected void initConfig() {
    super.initConfig();

    m_attachments = new HashMap<>();
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
  public void setAttachments(Collection<? extends BinaryResource> resources) {
    if (resources == null) {
      m_attachments = new HashMap<>(0);
      return;
    }
    Map<String, BinaryResource> newMap = new HashMap<>(resources.size());
    for (BinaryResource resource : resources) {
      if (resource != null) {
        newMap.put(resource.getFilename(), resource);
      }
    }
    m_attachments = newMap;
  }

  @Override
  public void addAttachment(BinaryResource resource) {
    if (resource != null) {
      m_attachments.put(resource.getFilename(), resource);
    }
  }

  @Override
  public void removeAttachment(BinaryResource resource) {
    if (resource != null) {
      m_attachments.remove(resource.getFilename());
    }
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
  public void setHtmlEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_HTML_ENABLED, enabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

}
