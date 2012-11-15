/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;

/**
 * convenience subclass of {@link AbstractDateColumn} with hasDate=false and hasTime=true
 */
public abstract class AbstractTimeColumn extends AbstractDateColumn {

  public AbstractTimeColumn() {
    super();
  }

  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredHasDate() {
    return false;
  }

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }

}
