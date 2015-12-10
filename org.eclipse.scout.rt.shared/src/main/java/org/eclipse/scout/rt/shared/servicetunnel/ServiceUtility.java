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
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.RuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.holders.HolderUtility;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.Assertions;
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
   * Invokes the given operation on the service.
   *
   * @return the result of the service invocation.
   * @throws RuntimeException
   *           if the service invocation failed. Hence, runtime exceptions are propagated, any other exception is
   *           translated into {@link PlatformException}.
   */
  public Object invoke(final Object service, final Method operation, final Object[] args) {
    Assertions.assertNotNull(service, "service is null");
    Assertions.assertNotNull(operation, "operation is null");

    try {
      return operation.invoke(service, args != null ? args : new Object[0]);
    }
    catch (final Throwable t) {
      throw BEANS.get(RuntimeExceptionTranslator.class).translate(t);
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
