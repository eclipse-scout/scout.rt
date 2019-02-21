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

import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * A composite with a binary resource and a state.
 */
public class Attachment implements IAttachment {

  private final BinaryResource m_binaryResource;
  private boolean m_uploaded = false;
  private boolean m_referenced = true;

  public Attachment(BinaryResource binaryResource) {
    this(binaryResource, false);
  }

  public Attachment(BinaryResource binaryResource, boolean uploaded) {
    m_binaryResource = binaryResource;
  }

  public BinaryResource getBinaryResource() {
    return m_binaryResource;
  }

  @Override
  public boolean isUploaded() {
    return m_uploaded;
  }

  @Override
  public void setUploaded(boolean uploaded) {
    m_uploaded = uploaded;
  }

  @Override
  public boolean isReferenced() {
    return m_referenced;
  }

  @Override
  public void setReferenced(boolean referenced) {
    m_referenced = referenced;
  }

}
