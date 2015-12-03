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
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;

public abstract class AbstractDateColumnExtension<OWNER extends AbstractDateColumn> extends AbstractColumnExtension<Date, OWNER> implements IDateColumnExtension<OWNER> {

  public AbstractDateColumnExtension(OWNER owner) {
    super(owner);
  }
}
