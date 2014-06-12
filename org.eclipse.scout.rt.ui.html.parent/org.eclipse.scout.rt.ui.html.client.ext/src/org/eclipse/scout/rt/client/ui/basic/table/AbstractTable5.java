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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.extension.client.ui.basic.table.AbstractExtensibleTable;

public class AbstractTable5 extends AbstractExtensibleTable implements ITable5 {
  private List<ITableControl> m_tableControls;

  public AbstractTable5() {
    this(true);
  }

  public AbstractTable5(boolean callInitializer) {
    super(false);

    m_tableControls = new LinkedList<ITableControl>();

    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public List<ITableControl> getControls() {
    return m_tableControls;
  }

}
