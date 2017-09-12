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
package org.eclipse.scout.rt.shared.extension;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * The abstract class of all extension delegations from extendible methods.
 */
public abstract class AbstractExtensionChain<EXTENSION> {

  private final ListIterator<? extends IExtension<?>> m_iterator;

  private final Class<?> m_filterClass;

  private boolean m_hasExtension;
  private EXTENSION m_currentExtension;

  public AbstractExtensionChain(List<? extends IExtension<?>> extensions, Class<? extends IExtension> filterClass) {
    m_filterClass = filterClass;
    m_iterator = extensions.listIterator();
  }

  protected boolean hasNext() {
    computeNext(true);
    return m_hasExtension;
  }

  protected EXTENSION next() {
    computeNext(false);
    if (!m_hasExtension) {
      throw new NoSuchElementException();
    }
    return m_currentExtension;
  }

  private void computeNext(boolean forceRewind) {
    m_hasExtension = false;
    int nextCount = 0;
    while (m_iterator.hasNext()) {
      IExtension<?> next = m_iterator.next();
      nextCount++;
      if (m_filterClass.isInstance(next)) {
        m_hasExtension = true;
        @SuppressWarnings("unchecked")
        EXTENSION extension = (EXTENSION) next;
        m_currentExtension = extension;
        break;
      }
    }

    // rewind
    if (!m_hasExtension || forceRewind) {
      for (int i = nextCount; i > 0; i--) {
        m_iterator.previous();
      }
    }
  }

  protected boolean hasPrevious() {
    computePrevious(true);
    return m_hasExtension;
  }

  protected EXTENSION previous() {
    computePrevious(false);
    if (!m_hasExtension) {
      throw new NoSuchElementException();
    }
    return m_currentExtension;
  }

  private void computePrevious(boolean forceRewind) {
    m_hasExtension = false;
    int previousCount = 0;
    while (m_iterator.hasPrevious()) {
      IExtension<?> previous = m_iterator.previous();
      previousCount++;
      if (m_filterClass.isInstance(previous)) {
        m_hasExtension = true;
        @SuppressWarnings("unchecked")
        EXTENSION extension = (EXTENSION) previous;
        m_currentExtension = extension;
        break;
      }
    }

    // rewind
    if (!m_hasExtension || forceRewind) {
      for (int i = previousCount; i > 0; i--) {
        m_iterator.next();
      }
    }
  }

  protected void callChain(MethodInvocation<?> methodInvocation, Object... arguments) {
    if (hasNext()) {
      EXTENSION nextExtension = next();
      MethodState methodState = new MethodState(CollectionUtility.arrayList(arguments));
      try {
        methodInvocation.callMethod(nextExtension);
        methodState.setReturnValue(methodInvocation.getReturnValue());
      }
      catch (RuntimeException e) {
        methodInvocation.setException(e);
        methodState.setException(e);
        throw e;
      }
      catch (Exception e) {
        methodInvocation.setException(e);
        methodState.setException(e);
      }
      finally {
        previous();
      }
    }
    else {
      throw new IllegalStateException("No more elements in chain.");
    }
  }

  @SuppressWarnings("squid:S00118")
  public abstract class MethodInvocation<RETURN_VALUE> {
    private Exception m_exception;
    private RETURN_VALUE m_returnValue;

    protected abstract void callMethod(EXTENSION next);

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
