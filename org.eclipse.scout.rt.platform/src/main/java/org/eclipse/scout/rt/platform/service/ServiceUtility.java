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
package org.eclipse.scout.rt.platform.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.HolderUtility;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.service.internal.AbstractHolderArgumentVisitor;

/**
 * Handle calls directly on current session (no remoting)
 */
public final class ServiceUtility {

  private ServiceUtility() {
  }

  /**
   * @param service
   * @param operation
   * @param paramTypes
   * @return the reflective service operation that can be called using {@link #invoke(Method,Object,Object[])}
   */
  public static Method getServiceOperation(Class<?> serviceClass, String operation, Class<?>[] paramTypes) {
    try {
      if (serviceClass == null) {
        throw new ProcessingException("service class is null");
      }
      return serviceClass.getMethod(operation, paramTypes);
    }
    catch (Throwable t) {
      throw BEANS.get(ProcessingExceptionTranslator.class).translate(t);
    }
  }

  /**
   * @param serviceOperation
   * @param service
   * @param callerArgs
   * @return the service result
   * @throws ProcessingException
   *           Invoke the service operation using reflection. The service supports OUT variables using {@link IHolder}
   *           objects
   */
  public static Object invoke(Method serviceOperation, Object service, Object[] callerArgs) {
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
   * Holders and nvpairs need to be copied as value clones. A smartfield for example is a holder and must not go to
   * backend. NVPairs with holder values ae replaced by NVPair with serializable holder arguments
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
   * Apply changed holder and {@link NVPair} values from updatedArgs to callerArgs
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
}
