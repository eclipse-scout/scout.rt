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

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IObjectColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Column holding Objects
 */
@ClassId("c6a6ffc3-d042-427b-abe5-8e4f288c358d")
public abstract class AbstractObjectColumn extends AbstractColumn<Object> implements IObjectColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()

  public AbstractObjectColumn() {
    super();
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    int c;
    Object o1 = getValue(r1);
    Object o2 = getValue(r2);

    if (o1 == null && o2 == null) {
      c = 0;
    }
    else if (o1 == null) {
      c = -1;
    }
    else if (o2 == null) {
      c = 1;
    }
    else if ((o1 instanceof Comparable) && (o2 instanceof Comparable) && o1.getClass().isAssignableFrom(o2.getClass())) {
      c = ((Comparable) o1).compareTo(o2);
    }
    else {
      c = StringUtility.compareIgnoreCase(o1.toString(), o2.toString());
    }
    return c;
  }

  /**
   * Empty no display text by default
   */
  @Override
  protected String formatValueInternal(ITableRow row, Object value) {
    return "";
  }

  protected static class LocalObjectColumnExtension<OWNER extends AbstractObjectColumn> extends LocalColumnExtension<Object, OWNER> implements IObjectColumnExtension<OWNER> {

    public LocalObjectColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IObjectColumnExtension<? extends AbstractObjectColumn> createLocalExtension() {
    return new LocalObjectColumnExtension<AbstractObjectColumn>(this);
  }
}
