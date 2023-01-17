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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since Build 202
 */

public class JavaTransferObject extends TransferObject {
  private final Object m_localObject;

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
    List<T> result = new ArrayList<>();
    Object localObject = getLocalObject();
    if (localObject != null) {
      if (type.isInstance(localObject)) {
        result.add((T) localObject);
      }
      else if (localObject instanceof Collection) {
        Collection c = (Collection) localObject;
        if (CollectionUtility.hasElements(c)) {
          for (Object o : c) {
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
