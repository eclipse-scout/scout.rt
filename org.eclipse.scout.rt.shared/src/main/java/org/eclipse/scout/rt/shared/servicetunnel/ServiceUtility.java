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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.holders.HolderUtility;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.eclipse.scout.rt.shared.servicetunnel.internal.AbstractHolderArgumentVisitor;

@ApplicationScoped
public class ServiceUtility {

  /**
   * @return the reflective service operation that can be called using {@link #invoke(Method,Object,Object[])}
   */
  public Method getServiceOperation(Class<?> serviceClass, String operation, Class<?>[] paramTypes) {
    Assertions.assertNotNull(serviceClass, "service class is null");
    try {
      return serviceClass.getMethod(operation, paramTypes);
    }
    catch (NoSuchMethodException | SecurityException e) {
      throw BEANS.get(ProcessingExceptionTranslator.class).translate(e);
    }
  }

  /**
   * @throws ProcessingException
   *           Invoke the service operation using reflection. The service supports OUT variables using {@link IHolder}
   *           objects
   */
  public Object invoke(Method serviceOperation, Object service, Object[] callerArgs) {
    Assertions.assertNotNull(serviceOperation, "serviceOperation is null");
    Assertions.assertNotNull(service, "service is null");
    if (callerArgs == null) {
      callerArgs = new Object[0];
    }
    try {
      final Object data = serviceOperation.invoke(service, callerArgs);
      return data;
    }
    catch (Exception e) {
      throw BEANS.get(ProcessingExceptionTranslator.class).translate(e)
          .withContextInfo("service.name", service.getClass())
          .withContextInfo("service.operation", serviceOperation.getName())
          .withContextInfo("service.args", VerboseUtility.dumpObjects(callerArgs));
    }
  }

  /**
   * Holders and nvpairs need to be copied as value clones. A smartfield for example is a holder and must not go to
   * backend. NVPairs with holder values ae replaced by NVPair with serializable holder arguments
   */
  public Object[] filterHolderArguments(Object[] callerArgs) {
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
  public Object[] extractHolderArguments(Object[] callerArgs) {
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
  public void updateHolderArguments(Object[] callerArgs, Object[] updatedArgs, final boolean clearNonOutArgs) {
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
