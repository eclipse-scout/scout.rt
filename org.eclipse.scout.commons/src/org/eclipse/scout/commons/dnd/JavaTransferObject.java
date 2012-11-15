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

/**
 * @since Build 202
 */

public class JavaTransferObject extends TransferObject {
  private Object m_localObject;

  public JavaTransferObject(Object bean) {
    m_localObject = bean;
  }

  @Override
  public boolean isLocalObject() {
    return true;
  }

  public Object getLocalObject() {
    return m_localObject;
  }

  @Override
  public String toString() {
    return "JavaTransferObject[localObject=" + m_localObject + "]";
  }
}
