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
package org.eclipse.scout.commons;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.scout.commons.beans.FastBeanInfo;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class BeanUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanUtility.class);

  private static final Object BEAN_INFO_CACHE_LOCK;
  private static final Map<CompositeObject/*Class,Class*/, FastBeanInfo> BEAN_INFO_CACHE;

  static {
    BEAN_INFO_CACHE_LOCK = new Object();
    BEAN_INFO_CACHE = new HashMap<CompositeObject, FastBeanInfo>();
  }

  private BeanUtility() {
  }

  /**
   * @return all properties of from up to (and excluding) to stopClazz, filtering with filter
   */
  public static Map<String, Object> getProperties(Object from, Class<?> stopClazz, IPropertyFilter filter) throws ProcessingException {
    HashMap<String, Object> map = new HashMap<String, Object>();
    try {
      FastPropertyDescriptor[] props = getFastPropertyDescriptors(from.getClass(), stopClazz, filter);
      for (int i = 0; i < props.length; i++) {
        FastPropertyDescriptor fromProp = props[i];
        Method readMethod = fromProp.getReadMethod();
        if (readMethod != null) {
          Object value = readMethod.invoke(from, (Object[]) null);
          map.put(fromProp.getName(), value);
        }
      }
    }
    catch (Exception e) {
      throw new ProcessingException("object: " + from, e);
    }
    return map;
  }

  /**
   * @param lenient
   *          true just logs warnings on exceptions, false throws exceptions
   *          set all properties on to, filtering with filter
   */
  public static void setProperties(Object to, Map<String, Object> map, boolean lenient, IPropertyFilter filter) throws ProcessingException {
    FastBeanInfo toInfo = getFastBeanInfo(to.getClass(), null);
    for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Object> entry = it.next();
      String name = entry.getKey();
      Object value = entry.getValue();
      try {
        FastPropertyDescriptor desc = toInfo.getPropertyDescriptor(name);
        if (desc != null && (filter == null || filter.accept(desc))) {
          Method writeMethod = desc.getWriteMethod();
          if (writeMethod != null) {
            writeMethod.invoke(to, new Object[]{TypeCastUtility.castValue(value, writeMethod.getParameterTypes()[0])});
          }
        }
      }
      catch (Exception e) {
        if (lenient) {
          LOG.warn("property " + name + " with value " + value, e);
        }
        else {
          throw new ProcessingException("property " + name + " with value " + value, e);
        }
      }
    }
  }

  /**
   * Get all property descriptors from this class up to (and excluding) stopClazz
   * <p>
   * Getting bean properties using {@link Introspector} can be very slow and time consuming.
   * <p>
   * This hi-speed property introspector only inspects bean names, types and read/write methods.
   * <p>
   * The results are cached for further speed optimization.
   */
  public static FastBeanInfo getFastBeanInfo(Class<?> beanClass, Class<?> stopClass) {
    if (beanClass == null) {
      return new FastBeanInfo(beanClass, stopClass);
    }
    synchronized (BEAN_INFO_CACHE_LOCK) {
      CompositeObject key = new CompositeObject(beanClass, stopClass);
      FastBeanInfo info = BEAN_INFO_CACHE.get(key);
      if (info == null) {
        info = new FastBeanInfo(beanClass, stopClass);
        BEAN_INFO_CACHE.put(key, info);
      }
      return info;
    }
  }

  /**
   * Clear the cache used by {@link #getBeanInfoEx(Class, Class)}
   */
  public static void clearFastBeanInfoCache() {
    synchronized (BEAN_INFO_CACHE_LOCK) {
      BEAN_INFO_CACHE.clear();
    }
  }

  /**
   * Get all properties from this class up to (and excluding) stopClazz
   * 
   * @param filter
   * @throws IntrospectionException
   */
  public static FastPropertyDescriptor[] getFastPropertyDescriptors(Class<?> clazz, Class<?> stopClazz, IPropertyFilter filter) throws IntrospectionException {
    FastBeanInfo info = getFastBeanInfo(clazz, stopClazz);
    FastPropertyDescriptor[] a = info.getPropertyDescriptors();
    ArrayList<FastPropertyDescriptor> filteredProperties = new ArrayList<FastPropertyDescriptor>(a.length);
    for (int i = 0; i < a.length; i++) {
      FastPropertyDescriptor pd = a[i];
      if (filter != null && !(filter.accept(pd))) {
        // ignore it
      }
      else {
        filteredProperties.add(pd);
      }
    }
    return filteredProperties.toArray(new FastPropertyDescriptor[filteredProperties.size()]);
  }

}
