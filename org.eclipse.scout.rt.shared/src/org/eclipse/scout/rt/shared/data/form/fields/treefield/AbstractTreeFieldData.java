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
package org.eclipse.scout.rt.shared.data.form.fields.treefield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

public abstract class AbstractTreeFieldData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  private ArrayList<TreeNodeData> m_rootList = new ArrayList<TreeNodeData>();

  public AbstractTreeFieldData() {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
  }

  public int getRootCount() {
    return m_rootList.size();
  }

  public List<TreeNodeData> getRoots() {
    return m_rootList;
  }

  public void setRoots(List<TreeNodeData> rootList) {
    m_rootList = rootList != null ? new ArrayList<TreeNodeData>(rootList) : new ArrayList<TreeNodeData>(0);
    setValueSet(true);
  }

}
