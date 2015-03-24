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
package org.eclipse.scout.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.beans.FastBeanInfo;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.HolderUtility;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ExceptionTranslator;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.service.internal.AbstractHolderArgumentVisitor;

/**
 * Handle calls directly on current sesseion (no remoting)
 */
public final class ServiceUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceUtility.class);

  private ServiceUtility() {
  }

  /**
   * see {@link INullService} and {@link SERVICES#getService(Class)}
   * <p>
   * Creates a void proxy for a service interface that does nothing and uses a classloader that return itself for every
   * query. This trick voids out the ServiceUse class type check.
   */
  public static final INullService NULL_SERVICE;

  static {
    INullService n = null;
    try {
      ClassLoader identityLoader = new ClassLoader(INullService.class.getClassLoader()) {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
          if (name.startsWith("java.lang.")) {
            return super.loadClass(name);
          }
          return INullService.class;
        }
      };
      n = (INullService) Proxy.newProxyInstance(identityLoader, new Class<?>[]{INullService.class}, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          return null;
        }
      });
    }
    catch (Throwable t) {
      //nop
    }
    NULL_SERVICE = n;
  }

  /**
   * Inject config.ini bean properties.
   * <p>
   * A config ini bean property is a config.ini entry in the format classOrInterfaceName#propertyName=value
   * <p>
   * Example: org.myproject.IMyService#endpointAddress=http://....
   * <p>
   * or
   * <p>
   * <br>
   * client.id=13 org.myproject.MyService#clientId=${client.id}
   * <p>
   * The property is set if service is a subtype of the config ini type.
   */
  public static void injectConfigProperties(Object service) {
    if (service == null) {
      return;
    }
    FastBeanInfo beanInfo = BeanUtility.getFastBeanInfo(service.getClass(), null);
    Map<String, String> map = ConfigIniUtility.getProperties(service.getClass());
    for (Map.Entry<String, String> e : map.entrySet()) {
      String name = e.getKey();
      String text = e.getValue();
      try {
        FastPropertyDescriptor propDesc = beanInfo.getPropertyDescriptor(name);
        Method setterMethod = propDesc.getWriteMethod();
        if (setterMethod != null) {
          Object value = TypeCastUtility.castValue(text, propDesc.getPropertyType());
          setterMethod.invoke(service, value);
        }
        else {
          LOG.warn("no setter for " + name + "=" + text + " on " + service.getClass());
        }
      }
      catch (Exception ex) {
        LOG.error("setting " + name + "=" + text + " on " + service.getClass(), ex);
      }
    }
  }

  /**
   * @param service
   * @param operation
   * @param paramTypes
   * @return the reflective service operation that can be called using {@link #invoke(Method,Object,Object[])}
   * @throws ProcessingException
   */
  public static Method getServiceOperation(Class<?> serviceClass, String operation, Class<?>[] paramTypes) throws ProcessingException {
    try {
      if (serviceClass == null) {
        throw new ProcessingException("service class is null");
      }
      return serviceClass.getMethod(operation, paramTypes);
    }
    catch (Throwable t) {
      throw OBJ.get(ExceptionTranslator.class).translate(t);
    }
  }

  /**
   * @param serviceOperation
   * @param service
   * @param callerArgs
   * @return the service result
   * @throws ProcessingException
   *           Invoke the service operation usign reflection. The service supports OUT variables using {@link IHolder}
   *           objects
   */
  public static Object invoke(Method serviceOperation, Object service, Object[] callerArgs) throws ProcessingException {
    try {
      if (serviceOperation == null) {
        throw new ProcessingException("serviceOperation is null");
      }
      if (service == null) {
        throw new ProcessingException("service is null");
      }
      if (callerArgs == null) {
        callerArgs = new Object[0];
      }
      Object data = serviceOperation.invoke(service, callerArgs);
      return data;
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable t) {
      if (t instanceof InvocationTargetException) {
        Throwable test = ((InvocationTargetException) t).getTargetException();
        if (test != null) {
          t = test;
        }
      }
      if (t instanceof ProcessingException) {
        throw (ProcessingException) t;
      }
      else {
        throw new ProcessingException("service: " + service.getClass() + ", operation: " + serviceOperation.getName() + ", args: " + VerboseUtility.dumpObjects(callerArgs), t);
      }
    }
  }

  /**
   * Holders and nvpairs need to be copied as value clones. A smartfield for
   * example is a holder and must not go to backend. NVPairs with holder values
   * ae replaced by NVPair with serializable holder arguments
   */
  public static Object[] filterHolderArguments(Object[] callerArgs) {
    Object[] serializableArgs = new Object[callerArgs.length];
    new AbstractHolderArgumentVisitor() {
      @SuppressWarnings("unchecked")
      @Override
      public void visitHolder(IHolder input, IHolder output) {
        if (!HolderUtility.containEqualValues(output, input)) {
          output.setValue(input.getValue());
        }
      }

      @Override
      public void visitOther(Object[] input, Object[] output, int index) {
        output[index] = input[index];
      }
    }.startVisiting(callerArgs, serializableArgs, 1, true);
    return serializableArgs;
  }

  /**
   * Extract holders and nvpairs in callerArgs (and eventually in sub-arrays)
   */
  public static Object[] extractHolderArguments(Object[] callerArgs) {
    Object[] holderArgs = new Object[callerArgs.length];
    new AbstractHolderArgumentVisitor() {
      @Override
      public void visitHolder(IHolder input, IHolder output) {
      }

      @Override
      public void visitOther(Object[] input, Object[] output, int index) {
      }
    }.startVisiting(callerArgs, holderArgs, 1, true);
    return holderArgs;
  }

  /**
   * Apply changed holder and nvpair values from updatedArgs to callerArgs
   *
   * @param clearNonOutArgs
   *          if true deletes calerArgs that aren't out parameters
   */
  @SuppressWarnings("unchecked")
  public static void updateHolderArguments(Object[] callerArgs, Object[] updatedArgs, final boolean clearNonOutArgs) {
    if (updatedArgs != null) {
      new AbstractHolderArgumentVisitor() {
        @Override
        public void visitHolder(IHolder input, IHolder output) {
          if (!HolderUtility.containEqualValues(output, input)) {
            output.setValue(input.getValue());
          }
        }

        @Override
        public void visitOther(Object[] input, Object[] output, int index) {
          if (clearNonOutArgs) {
            output[index] = null;
          }
        }
      }.startVisiting(updatedArgs, callerArgs, 1, false);
    }
  }

  /**
   * @see BeanUtility#getInterfacesHierarchy(Class, Class)
   */
  public static Class[] getInterfacesHierarchy(Class type, Class filterClass) {
    return BeanUtility.getInterfacesHierarchy(type, filterClass);
  }

}
