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
 * convenience subclass of {@link AbstractDateColumn} with hasDate=true and hasTime=true
 */
@ClassId("8bbeefe2-84a8-43cf-8a4f-ce4d4845829e")
public abstract class AbstractDateTimeColumn extends AbstractDateColumn {

  public AbstractDateTimeColumn() {
    this(true);
  }

  public AbstractDateTimeColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }
}
