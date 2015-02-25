/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.dnd;

public class TransferObjectRequest {

  private final Class<? extends TransferObject> m_transferObjectType;
  private final String m_mimeType;

  public TransferObjectRequest(Class<? extends TransferObject> objectType) {
    this(objectType, null);
  }

  public TransferObjectRequest(Class<? extends TransferObject> objectType, String type) {
    m_transferObjectType = objectType;
    m_mimeType = type;
  }

  public Class<? extends TransferObject> getTransferObjectType() {
    return m_transferObjectType;
  }

  public String getMimeType() {
    return m_mimeType;
  }
}
