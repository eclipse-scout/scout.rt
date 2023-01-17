/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.bookmark;

import java.io.Serializable;

public abstract class AbstractPageState implements Serializable {
  private static final long serialVersionUID = 1L;

  private String m_bookmarkIdentifier;
  private String m_pageClassName;
  private String m_label;
  // since 03.07.2009
  private Boolean m_expanded;

  protected AbstractPageState() {
  }

  protected AbstractPageState(AbstractPageState state) {
    this.m_pageClassName = state.m_pageClassName;
    this.m_bookmarkIdentifier = state.m_bookmarkIdentifier;
    this.m_label = state.m_label;
  }

  public String getPageClassName() {
    return m_pageClassName;
  }

  public void setPageClassName(String s) {
    m_pageClassName = s;
  }

  public String getBookmarkIdentifier() {
    return m_bookmarkIdentifier;
  }

  public void setBookmarkIdentifier(String id) {
    m_bookmarkIdentifier = id;
  }

  public String getLabel() {
    return m_label;
  }

  public void setLabel(String s) {
    m_label = s;
  }

  public Boolean isExpanded() {
    return m_expanded;
  }

  public void setExpanded(Boolean b) {
    m_expanded = b;
  }

  /**
   * Creates a copy of this instance. The copy is basically a deep copy, but resource intensive references like byte
   * arrays containing serialized data as well as immutable objects are shallow copied.
   */
  public abstract AbstractPageState copy();
}
