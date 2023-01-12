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

/**
 * This is the super class of a transfer object model, normally this type is not used directly but any subtype of it.
 */
public class TransferObject {

  private String m_mimeType;

  public String getMimeType() {
    return m_mimeType;
  }

  public void setMimeType(String mimeType) {
    m_mimeType = mimeType;
  }
}
