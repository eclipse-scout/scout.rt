/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.cdi.internal;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.platform.cdi.Instance;

/**
 *
 */
public class InstanceImpl<T> implements Instance<T> {

  private final List<T> m_elements;

  public InstanceImpl(List<T> elements) {
    m_elements = elements;
  }

  @Override
  public Iterator<T> iterator() {
    return m_elements.iterator();
  }

  @Override
  public boolean isAmbiguous() {
    return false;
  }

}
