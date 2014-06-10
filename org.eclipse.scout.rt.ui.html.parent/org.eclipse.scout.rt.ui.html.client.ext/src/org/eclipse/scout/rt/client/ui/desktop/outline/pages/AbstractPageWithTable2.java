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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;

public class AbstractPageWithTable2<T extends ITable> extends AbstractPageWithTable<T> implements IPage2 {
  private List<ITableControl> m_tableControls;

  public AbstractPageWithTable2() {
    this(true);
  }

  public AbstractPageWithTable2(boolean callInitializer) {
    super(false);

    m_tableControls = new LinkedList<ITableControl>();

    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public List<ITableControl> getTableControls() {
    return m_tableControls;
  }
}
