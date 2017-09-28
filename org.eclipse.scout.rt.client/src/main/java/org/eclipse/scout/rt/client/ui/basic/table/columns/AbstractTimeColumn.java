/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

}
