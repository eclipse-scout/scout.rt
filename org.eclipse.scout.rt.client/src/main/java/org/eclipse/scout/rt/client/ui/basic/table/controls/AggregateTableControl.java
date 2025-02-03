/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.AbstractIcons;

@ClassId("94a7bf28-8c9a-4b27-8edd-151c5620d987")
@Order(600)
public class AggregateTableControl extends AbstractTableControl implements IAggregateTableControl {

  public AggregateTableControl() {
    this(true);
  }

  public AggregateTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTooltipText(TEXTS.get("ui.Total"));
    setIconId(AbstractIcons.Sum);
  }
}
