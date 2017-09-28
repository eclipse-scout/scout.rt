/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.form.fields.browserfield;

import java.util.Set;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

public abstract class AbstractBrowserFieldData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  private String m_location;
  private BinaryResource m_binaryResource;
  private Set<BinaryResource> m_attachments;

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
}
