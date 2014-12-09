/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;

/**
 * The abstract class of all extension delegations from extendible methods.
 */
public abstract class AbstractExtensionChain<EXTENSION> {

  private final ListIterator<? extends EXTENSION> m_iterator;

  /**
   * the execution state of every executer.
   */
  private final Map<EXTENSION, MethodState> m_executionStates = new HashMap<EXTENSION, MethodState>();

  public AbstractExtensionChain(List<? extends EXTENSION> executers) {
    m_iterator = executers.listIterator();

  }

  protected boolean hasNext() {
    return m_iterator.hasNext();
  }

  protected EXTENSION next() {
    return m_iterator.next();
  }

  protected boolean hasPrevious() {
    return m_iterator.hasPrevious();
  }

  protected EXTENSION previous() {
    return m_iterator.previous();
  }

  protected void callChain(MethodInvocation<?> methodInvocation, Object... arguments) {
    if (hasNext()) {
      EXTENSION nextExtension = next();
      MethodState methodState = new MethodState(CollectionUtility.arrayList(arguments));
      try {
        m_executionStates.put(nextExtension, methodState);
        methodInvocation.callMethod(nextExtension);
        methodState.setReturnValue(methodInvocation.getReturnValue());
      }
      catch (Exception e) {
        methodInvocation.setException(e);
        methodState.setException(e);
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
      }
      finally {
        previous();
      }

    }
    else {
      throw new IllegalStateException("No more elements in chain.");
    }
  }

  public abstract class MethodInvocation<RETURN_VALUE> {
    private Exception m_exception;
    private RETURN_VALUE m_returnValue;

    protected abstract void callMethod(EXTENSION next) throws Exception;

    public void setException(Exception exception) {
      m_exception = exception;
    }

    public Exception getException() {
      return m_exception;
    }

    protected void setReturnValue(RETURN_VALUE returnValue) {
      m_returnValue = returnValue;
    }

    public RETURN_VALUE getReturnValue() {
      return m_returnValue;
    }

  }
}
