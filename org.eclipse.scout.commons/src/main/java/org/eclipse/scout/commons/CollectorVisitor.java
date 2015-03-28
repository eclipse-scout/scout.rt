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
package org.eclipse.scout.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor which collects all visited elements.
 *
 * @since 5.1
 */
public class CollectorVisitor<ELEMENT> implements IVisitor<ELEMENT> {

  private final List<ELEMENT> m_elements;

  public CollectorVisitor() {
    m_elements = new ArrayList<>();
  }

  /**
   * @return visited elements.
   */
  public List<ELEMENT> getElements() {
    return m_elements;
  }

  @Override
  public boolean visit(final ELEMENT element) {
    m_elements.add(element);
    return true;
  }
}
