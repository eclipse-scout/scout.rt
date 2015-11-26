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
package org.eclipse.scout.rt.client.ui.dnd;

/**
 * @since Build 202
 */

public class ImageTransferObject extends TransferObject {
  private Object m_img;

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
