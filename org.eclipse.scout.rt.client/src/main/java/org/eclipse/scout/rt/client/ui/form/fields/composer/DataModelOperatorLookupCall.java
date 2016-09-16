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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Convenience lookup call to present {@link IDataModelAttribute#getOperators()}
 * <p>
 * This lookup call expects the property {@link #setAttribute(IDataModelAttribute)} to be set.
 */
@ClassId("5692346e-a059-45ff-a287-781319c00d6a")
public class DataModelOperatorLookupCall extends LocalLookupCall<IDataModelAttributeOp> {
  private static final long serialVersionUID = 1L;

  private IDataModelAttribute m_attribute;

  public void setAttribute(IDataModelAttribute attribute) {
    m_attribute = attribute;
  }

  public IDataModelAttribute getAttribute() {
    return m_attribute;
  }

  @Override
  protected List<ILookupRow<IDataModelAttributeOp>> execCreateLookupRows() {
    List<ILookupRow<IDataModelAttributeOp>> result = new ArrayList<ILookupRow<IDataModelAttributeOp>>();
    List<IDataModelAttributeOp> ops = null;
    if (m_attribute != null) {
      ops = m_attribute.getOperators();
    }
    if (ops != null) {
      for (IDataModelAttributeOp op : ops) {
        String text = op.getShortText();
        if (text != null && text.indexOf("{0}") >= 0) {
          text = text.replace("{0}", "n");
        }
        if (text != null && text.indexOf("{1}") >= 0) {
          text = text.replace("{1}", "m");
        }
        result.add(new LookupRow<IDataModelAttributeOp>(op, text));
      }
    }

    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_attribute == null) ? 0 : m_attribute.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DataModelOperatorLookupCall other = (DataModelOperatorLookupCall) obj;
    if (m_attribute == null) {
      if (other.m_attribute != null) {
        return false;
      }
    }
    else if (!m_attribute.equals(other.m_attribute)) {
      return false;
    }
    return true;
  }
}
