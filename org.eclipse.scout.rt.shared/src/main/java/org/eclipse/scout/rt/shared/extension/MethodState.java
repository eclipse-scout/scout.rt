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

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class MethodState {

  private Object m_returnValue;
  private final List<?> m_parameters;
  private Exception m_exception;

  public MethodState() {
    this(CollectionUtility.emptyArrayList());
  }

  public MethodState(List<?> parameters) {
    m_parameters = CollectionUtility.arrayList(parameters);
  }

  public List<?> getParameters() {
    return m_parameters;
  }

  public void setReturnValue(Object returnValue) {
    m_returnValue = returnValue;
  }

  public Object getReturnValue() {
    return m_returnValue;
  }

  public void setException(Exception exception) {
    m_exception = exception;
  }

  public Exception getException() {
    return m_exception;
  }

}
