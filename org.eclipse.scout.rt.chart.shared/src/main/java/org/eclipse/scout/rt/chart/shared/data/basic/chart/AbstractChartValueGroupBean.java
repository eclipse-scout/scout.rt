/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.annotations.IgnoreProperty;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public abstract class AbstractChartValueGroupBean implements IChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private String m_type;
  private Object m_groupKey;
  private String m_groupName;
  private List<String> m_colorHexValue = CollectionUtility.emptyArrayList();
  private boolean m_clickable = true;

  @Override
  public String getType() {
    return m_type;
  }

  @Override
  public void setType(String type) {
    m_type = type;
  }

  @Override
  @IgnoreProperty
  public Object getGroupKey() {
    return m_groupKey;
  }

  @Override
  @IgnoreProperty
  public void setGroupKey(Object groupKey) {
    m_groupKey = groupKey;
  }

  @Override
  public String getGroupName() {
    return m_groupName;
  }

  @Override
  public void setGroupName(String groupName) {
    m_groupName = groupName;
  }

  @Override
  public List<String> getColorHexValue() {
    return Collections.unmodifiableList(getColorHexValueInternal());
  }

  protected List<String> getColorHexValueInternal() {
    return m_colorHexValue;
  }

  @Override
  public void setColorHexValue(String... colorHexValue) {
    setColorHexValue(CollectionUtility.arrayList(colorHexValue));
  }

  @Override
  public void setColorHexValue(List<String> colorHexValue) {
    setColorHexValueInternal(CollectionUtility.arrayList(colorHexValue));
  }

  protected void setColorHexValueInternal(List<String> colorHexValue) {
    m_colorHexValue = colorHexValue;
  }

  @Override
  public void addColorHexValue(String colorHexValue) {
    getColorHexValueInternal().add(colorHexValue);
  }

  @Override
  public void clearColorHexValue() {
    getColorHexValueInternal().clear();
  }

  @Override
  public boolean isClickable() {
    return m_clickable;
  }

  @Override
  public void setClickable(boolean clickable) {
    this.m_clickable = clickable;
  }
}
