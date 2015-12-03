/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

/**
 * convenience subclass of {@link AbstractDateColumn} with hasDate=true and hasTime=true
 */
public abstract class AbstractDateTimeColumn extends AbstractDateColumn {

  public AbstractDateTimeColumn() {
    super();
  }

  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }

}
