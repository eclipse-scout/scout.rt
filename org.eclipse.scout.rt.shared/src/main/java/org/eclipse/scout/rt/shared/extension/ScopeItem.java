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

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;

/**
 * A scope item represents a part of a {@link ClassIdentifier}, where a part is starting from a particular index and
 * ends either at the class identifier's last or first segment. The one ending at the last segment is called default or
 * forward traversing scope item (created using {@link #createSubScopeItem()}) whereas the one ending at the first
 * segment is called reverse traversing scope item (created using
 * {@link #createReverseTraversingScopeItem(ClassIdentifier)}).
 * <p/>
 * A scope item can create a sub scope item. Depending on the traversing strategy, the sub scope item looses its segment
 * at the beginning or at the end.
 * <p/>
 * Instances of this class are immutable.
 */
public class ScopeItem {

  private final ClassIdentifier m_identifier;
  private final boolean m_topDownStrategy;
  private final int m_index;

  public ScopeItem(ClassIdentifier identifier, boolean topDownStrategy) {
    this(identifier, topDownStrategy ? 0 : identifier.size() - 1, topDownStrategy);
  }

  private ScopeItem(ClassIdentifier identifier, int index, boolean topDownStrategy) {
    m_identifier = identifier;
    m_index = index;
    m_topDownStrategy = topDownStrategy;
  }

  /**
   * @return Returns the {@link ClassIdentifier}'s segment referenced by this scope item.
   */
  public Class<?> getCurrentSegment() {
    return m_identifier.getClasses()[m_index];
  }

  /**
   * @return Returns <code>true</code> if the last segment has been met. (Depending on the traversing strategy the last
   *         segment is the class identifier's first or last segment).
   */
  public boolean isLastSegment() {
    if (m_topDownStrategy) {
      return m_index == m_identifier.size() - 1;
    }
    return m_index == 0;
  }

  public ClassIdentifier getIdentifier() {
    return m_identifier;
  }

  /**
   * @return Returns a new {@link ScopeItem}.
   */
  public ScopeItem createSubScopeItem() {
    if (isLastSegment()) {
      return null;
    }
    if (m_topDownStrategy) {
      return new ScopeItem(getIdentifier(), m_index + 1, true);
    }
    return new ScopeItem(getIdentifier(), m_index - 1, false);
  }

  @Override
  public String toString() {
    return "ScopeItem [m_identifier=" + m_identifier + ", m_index=" + m_index + "]";
  }
}
