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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.IOrdered;

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

  public MultiStatus() {
    super(null);
  }

  public MultiStatus(IMultiStatus ms) {
    this();
    Assertions.assertNotNull(ms);
    for (IStatus s : ms.getChildren()) {
      add(s);
    }
  }

  @Override
  public int getSeverity() {
    if (m_children.size() > 0) {
      return m_children.first().getSeverity();
    }
    return IStatus.OK;
  }

  @Override
  public double getOrder() {
    if (m_children.size() > 0) {
      return m_children.first().getOrder();
    }
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * The messages of the children with max severity and priority, or an empty message, if no children are available.
   */
  @Override
  public String getMessage() {
    return formatMessages(filterSameOrder(filterSameSeverity(m_children)));
  }

  private List<IStatus> filterSameOrder(Collection<IStatus> statuses) {
    List<IStatus> res = new ArrayList<>();
    for (IStatus s : statuses) {
      if (s.getOrder() != getOrder()) {
        break;
      }
      res.add(s);
    }
    return res;
  }

  private List<IStatus> filterSameSeverity(Collection<IStatus> statuses) {
    List<IStatus> res = new ArrayList<>();
    for (IStatus s : statuses) {
      if (s.getSeverity() != getSeverity()) {
        break;
      }
      res.add(s);
    }
    return res;
  }

  protected String formatMessages(List<IStatus> statuses) {
    List<String> messages = new ArrayList<>();
    for (IStatus s : statuses) {
      messages.add(s.getMessage());
    }
    return StringUtility.join("\n", messages);
  }

  /**
   * Adds the given status to this multi-status. Sets the severity to the maximum severity of the children and the
   * message to the message of the max Severity.
   *
   * @param status
   *          the new child status, not <code>null</code>
   */
  public void add(IStatus status) {
    m_children.add(Assertions.assertNotNull(status));
  }

  public void addAll(List<IStatus> status) {
    m_children.addAll(Assertions.assertNotNull(status));
  }

  public void remove(IStatus status) {
    m_children.remove(Assertions.assertNotNull(status));
  }

  /**
   * Remove all children with the given class
   */
  @Override
  public void removeAll(Class<? extends IStatus> clazz) {
    for (IStatus child : getChildren()) {
      if (Assertions.assertNotNull(clazz).isAssignableFrom(child.getClass())) {
        m_children.remove(Assertions.assertNotNull(child));
      }
    }
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
  public boolean containsStatus(Class<? extends IStatus> clazz) {
    Assertions.assertNotNull(clazz);
    for (IStatus child : getChildren()) {
      if (clazz.isAssignableFrom(child.getClass())
          || child instanceof IMultiStatus && ((IMultiStatus) child).containsStatus(clazz)) {
        return true;
      }
    }
    return false;
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
