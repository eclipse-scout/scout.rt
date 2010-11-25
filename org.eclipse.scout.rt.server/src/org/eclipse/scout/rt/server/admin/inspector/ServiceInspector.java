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
package org.eclipse.scout.rt.server.admin.inspector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.IServiceInventory;

public class ServiceInspector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceInspector.class);

  private Object m_service;
  private ReflectServiceInventory m_inv;

  public ServiceInspector(Object service) {
    m_service = service;
  }

  public ReflectServiceInventory buildInventory() {
    ReflectServiceInventory inv = new ReflectServiceInventory(m_service);
    // make service inventory
    if (m_service instanceof IAdaptable) {
      IServiceInventory si = (IServiceInventory) ((IAdaptable) m_service).getAdapter(IServiceInventory.class);
      if (si != null) inv.addState(si.getInventory());
    }
    return inv;
  }

  public Object getService() {
    return m_service;
  }

  public void changeProperty(PropertyDescriptor propDesc, String propText) throws Exception {
    if (propText != null && propText.length() == 0) propText = null;
    Method setterMethod = propDesc.getWriteMethod();
    if (setterMethod != null) {
      Object value = TypeCastUtility.castValue(propText, propDesc.getPropertyType());
      setterMethod.invoke(m_service, new Object[]{value});
    }
  }

}
