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
 * convenience subclass of {@link AbstractDateColumn} with hasDate=false and hasTime=true
 */
@ClassId("beec005a-d733-42d0-ab8d-a9d4e43deeb4")
public abstract class AbstractTimeColumn extends AbstractDateColumn {

  public AbstractTimeColumn() {
    this(true);
  }

  public AbstractTimeColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredHasDate() {
    return false;
  }

  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }

  @Override
  protected String getConfiguredGroupFormat() {
    return "HH";
  }
}
