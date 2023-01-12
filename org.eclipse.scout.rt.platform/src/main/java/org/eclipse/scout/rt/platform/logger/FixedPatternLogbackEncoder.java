/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

/**
 * Convenience encoder so that {@link LayoutWrappingEncoder} must not be used manually in logback.xml.
 *
 * @see FixedPatternLogbackLayout
 */
public class FixedPatternLogbackEncoder extends LayoutWrappingEncoder<ILoggingEvent> {

  protected List<String> m_mdcRenames = new ArrayList<>();
  protected List<String> m_mdcExclusions = new ArrayList<>();
  protected List<String> m_mdcInclusions = new ArrayList<>();
  protected List<String> m_mdcOrders = new ArrayList<>();

  /**
   * @see FixedPatternLogbackLayout#addMdcRename(String)
   */
  public void addMdcRename(String mdcRename) {
    m_mdcRenames.add(mdcRename);
  }

  /**
   * @see FixedPatternLogbackLayout#addMdcExclusion(String)
   */
  public void addMdcExclusion(String mdcKey) {
    m_mdcExclusions.add(mdcKey);
  }

  /**
   * @see FixedPatternLogbackLayout#addMdcInclusion(String)
   */
  public void addMdcInclusion(String mdcKey) {
    m_mdcInclusions.add(mdcKey);
  }

  /**
   * @see FixedPatternLogbackLayout#addMdcOrder(String)
   */
  public void addMdcOrder(String mdcKey) {
    m_mdcOrders.add(mdcKey);
  }

  @Override
  public void start() {
    FixedPatternLogbackLayout layout = new FixedPatternLogbackLayout();
    layout.setContext(context);

    m_mdcRenames.forEach(layout::addMdcRename);
    m_mdcExclusions.forEach(layout::addMdcExclusion);
    m_mdcInclusions.forEach(layout::addMdcInclusion);
    m_mdcOrders.forEach(layout::addMdcOrder);

    layout.start();
    this.layout = layout;
    super.start();
  }

  @Override
  public void setLayout(Layout<ILoggingEvent> layout) {
    throw new UnsupportedOperationException("one cannot set the layout of " + this.getClass().getName());
  }
}
