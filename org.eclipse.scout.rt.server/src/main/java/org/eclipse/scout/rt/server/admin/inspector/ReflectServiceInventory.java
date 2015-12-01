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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.util.CompositeObject;

/**
 * Convenience for use with {@link IInventoryProvider}
 */
public class ReflectServiceInventory {
  private HashSet<PropertyDescriptor> m_properties = new HashSet<PropertyDescriptor>();
  private HashSet<Method> m_operations = new HashSet<Method>();
  private ArrayList<String> m_states = new ArrayList<String>();

  public ReflectServiceInventory(Object service) {
    analyzeService(service);
  }

  private void analyzeService(Object service) {
    // properties
    HashSet<Method> propertyMethods = new HashSet<Method>();
    try {
      BeanInfo info = Introspector.getBeanInfo(service.getClass());
      PropertyDescriptor[] a = info.getPropertyDescriptors();
      for (PropertyDescriptor desc : a) {
        if (desc.getReadMethod() != null && desc.getWriteMethod() != null) {
          addProperty(desc);
        }
        else if (desc.getName().startsWith("configured")) {
          addProperty(desc);
        }
        if (desc.getReadMethod() != null) {
          propertyMethods.add(desc.getReadMethod());
        }
        if (desc.getWriteMethod() != null) {
          propertyMethods.add(desc.getWriteMethod());
        }
      }
    }
    catch (Exception e) {
      m_states.add("Failure to analyze properties: " + e);
    }
    // operations
    Method[] methods = service.getClass().getMethods();
    for (Method m : methods) {
      if (Object.class.isAssignableFrom(m.getDeclaringClass())) {
        if (!propertyMethods.contains(m)) {
          addOperation(m);
        }
      }
    }
  }

  public PropertyDescriptor[] getProperties() {
    TreeMap<CompositeObject, PropertyDescriptor> sortMap = new TreeMap<CompositeObject, PropertyDescriptor>();
    int index = 0;
    for (PropertyDescriptor p : m_properties) {
      if (p.getName().equals("class")) {
        // ignore
      }
      else {
        if (p.getName().startsWith("configured")) {
          sortMap.put(new CompositeObject(1, p.getName(), index), p);
        }
        else {
          sortMap.put(new CompositeObject(2, p.getName(), index), p);
        }
      }
      index++;
    }
    return sortMap.values().toArray(new PropertyDescriptor[0]);
  }

  public void addProperty(PropertyDescriptor desc) {
    m_properties.add(desc);
  }

  public void removeProperty(PropertyDescriptor desc) {
    m_properties.remove(desc);
  }

  public Method[] getOperations() {
    TreeMap<CompositeObject, Method> sortMap = new TreeMap<CompositeObject, Method>();
    int index = 0;
    for (Method m : m_operations) {
      if (m.getName().equals("getInventory")) {
        // ignore
      }
      else {
        if (m.getName().startsWith("exec")) {
          sortMap.put(new CompositeObject(1, m.getName(), index), m);
        }
        else {
          sortMap.put(new CompositeObject(2, m.getName(), index), m);
        }
      }
      index++;
    }
    return sortMap.values().toArray(new Method[0]);
  }

  public void addOperation(Method op) {
    m_operations.add(op);
  }

  public void removeOperation(Method op) {
    m_operations.remove(op);
  }

  public String[] getStates() {
    return m_states.toArray(new String[0]);
  }

  public void addState(String text) {
    m_states.add(text);
  }

}
