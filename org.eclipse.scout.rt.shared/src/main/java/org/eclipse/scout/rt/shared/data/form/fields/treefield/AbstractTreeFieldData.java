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
package org.eclipse.scout.rt.shared.data.form.fields.treefield;

import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

public abstract class AbstractTreeFieldData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  private List<TreeNodeData> m_rootList;

  public int getRootCount() {
    return CollectionUtility.size(m_rootList);
  }

  public List<TreeNodeData> getRoots() {
    return CollectionUtility.arrayList(m_rootList);
  }

  public void setRoots(List<TreeNodeData> rootList) {
    if (rootList == null) {
      m_rootList = null;
    }
    else {
      m_rootList = CollectionUtility.arrayList(rootList);
    }
    setValueSet(true);
  }
}
