/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fields.browserfield;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

public abstract class AbstractBrowserFieldData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  private String m_location;
  private BinaryResource m_binaryResource;
  private Set<BinaryResource> m_attachments;
  private Map<String, ContentSecurityPolicy> m_csp;

  @Override
  public Class<?> getFieldStopClass() {
    return AbstractBrowserFieldData.class;
  }

  public String getLocation() {
    return m_location;
  }

  public void setLocation(String location) {
    m_location = location;
    setValueSet(true);
  }

  public BinaryResource getBinaryResource() {
    return m_binaryResource;
  }

  public void setBinaryResource(BinaryResource binaryResource) {
    m_binaryResource = binaryResource;
    setValueSet(true);
  }

  public Set<BinaryResource> getAttachments() {
    return m_attachments;
  }

  public void setAttachments(Set<BinaryResource> attachments) {
    m_attachments = attachments;
    setValueSet(true);
  }

  public Map<String, ContentSecurityPolicy> getContentSecurityPolicy() {
    return m_csp;
  }

  public void setContentSecurityPolicy(Map<String, ContentSecurityPolicy> csp) {
    m_csp = csp;
    setValueSet(true);
  }

  public void putContentSecurityPolicy(String key, ContentSecurityPolicy csp) {
    if (m_csp == null) {
      m_csp = new HashMap<>();
    }
    m_csp.put(key, csp);
    setValueSet(true);
  }
}
