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
    if (propText != null && propText.length() == 0) {
      propText = null;
    }
    Method setterMethod = propDesc.getWriteMethod();
    if (setterMethod != null) {
      Object value = TypeCastUtility.castValue(propText, propDesc.getPropertyType());
      setterMethod.invoke(m_service, new Object[]{value});
    }
  }
}
