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
 * @since Build 202
 */

public class ImageTransferObject extends TransferObject {
  private final Object m_img;

  public ImageTransferObject(Object img) {
    m_img = img;
  }

  public Object getImage() {
    return m_img;
  }

  @Override
  public String toString() {
    return "ImageTransferObject[image=" + m_img + "]";
  }
}
