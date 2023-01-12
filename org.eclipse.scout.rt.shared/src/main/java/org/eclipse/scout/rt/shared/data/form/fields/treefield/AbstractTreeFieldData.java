/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
