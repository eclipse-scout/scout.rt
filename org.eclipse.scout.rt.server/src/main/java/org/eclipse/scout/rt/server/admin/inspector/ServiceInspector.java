/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.inspector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.service.IServiceInventory;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

public class ServiceInspector {

  private final IService m_service;

  public ServiceInspector(IService service) {
    m_service = service;
  }

  public ReflectServiceInventory buildInventory() {
    ReflectServiceInventory inv = new ReflectServiceInventory(m_service);
    if (m_service instanceof IServiceInventory) {
      // make service inventory
      IServiceInventory si = (IServiceInventory) m_service;
      inv.addState(si.getInventory());
    }
    return inv;
  }

  public IService getService() {
    return m_service;
  }

  public void changeProperty(PropertyDescriptor propDesc, String propText) throws IllegalAccessException, InvocationTargetException {
    if (propText != null && propText.isEmpty()) {
      propText = null;
    }
    Method setterMethod = propDesc.getWriteMethod();
    if (setterMethod != null) {
      Object value = TypeCastUtility.castValue(propText, propDesc.getPropertyType());
      setterMethod.invoke(m_service, new Object[]{value});
    }
  }
}
