/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * A column that renders its content as a font icon or image
 */
@ClassId("d5beef55-b40e-44f6-a539-be3bd75d3e33")
public abstract class AbstractIconColumn extends AbstractColumn<String> implements IIconColumn {

  public AbstractIconColumn() {
    this(true);
  }

  public AbstractIconColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected int getConfiguredWidth() {
    return NARROW_MIN_WIDTH;
  }

  @Override
  protected int getConfiguredMinWidth() {
    return NARROW_MIN_WIDTH;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0;
  }
}
