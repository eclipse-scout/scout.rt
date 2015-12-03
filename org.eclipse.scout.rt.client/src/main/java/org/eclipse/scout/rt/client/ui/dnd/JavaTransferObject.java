/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.dnd;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since Build 202
 */

public class JavaTransferObject extends TransferObject {
  private Object m_localObject;

  public JavaTransferObject(Object bean) {
    m_localObject = bean;
  }

  public Object getLocalObject() {
    return m_localObject;
  }

  @Override
  public String toString() {
    return "JavaTransferObject[localObject=" + m_localObject + "]";
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getLocalObjectAsList(Class<T> type) {
    List<T> result = new ArrayList<T>();
    Object localObject = getLocalObject();
    if (localObject != null) {
      if (type.isInstance(localObject)) {
        result.add((T) localObject);
      }
      else if (localObject instanceof Collection) {
        Collection c = (Collection) localObject;
        if (CollectionUtility.hasElements(c)) {
          Iterator it = c.iterator();
          while (it.hasNext()) {
            Object o = it.next();
            if (type.isInstance(o)) {
              result.add((T) o);
            }
          }
        }
      }
      else if (localObject.getClass().isArray()) {
        int length = Array.getLength(localObject);
        for (int i = 0; i < length; i++) {
          Object o = Array.get(localObject, i);
          if (type.isInstance(o)) {
            result.add((T) o);
          }

        }
      }
    }
    return result;
  }
}
