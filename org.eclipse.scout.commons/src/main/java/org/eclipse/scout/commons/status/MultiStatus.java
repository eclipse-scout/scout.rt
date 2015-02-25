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
package org.eclipse.scout.commons.status;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;

/**
 * Implementation of a {@link IMultiStatus}: A status with child statuses.
 * <p>
 * The severity given by maximum severity of the children or {@link #IStatus#OK}, if no children available.
 * </p>
 */
public class MultiStatus extends Status implements IMultiStatus {
  private static final long serialVersionUID = -4166865994075130263L;

  //children ordered by severity and message
  private final SortedSet<IStatus> m_children = new TreeSet<>();

  public MultiStatus(String message) {
    super(message, IStatus.OK);
  }

  public MultiStatus() {
    this(null);
  }

  /**
   * @return maximum severity of the children or {@link #DEFAULT_SEVERITY}, if no children available
   */
  @Override
  public int getSeverity() {
    if (m_children.size() > 0) {
      return m_children.first().getSeverity();
    }
    return IStatus.OK;
  }

  /**
   * Adds the given status to this multi-status.
   * Sets the severity to the maximum severity of the children and the message to the message of the max Severity.
   *
   * @param status
   *          the new child status, not <code>null</code>
   */
  public void add(IStatus status) {
    m_children.add(Assertions.assertNotNull(status));
  }

  @Override
  public boolean isMultiStatus() {
    return true;
  }

  @Override
  public List<IStatus> getChildren() {
    return CollectionUtility.arrayList(m_children);
  }

  @Override
  public String toString() {
    return "MultiStatus [severity=" + getSeverity() + ", message=" + getMessage() + ", children=" + m_children + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_children == null) ? 0 : m_children.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MultiStatus other = (MultiStatus) obj;
    if (m_children == null) {
      if (other.m_children != null) {
        return false;
      }
    }
    else if (!CollectionUtility.equalsCollection(m_children, other.m_children)) {
      return false;
    }

    return true;
  }

}
