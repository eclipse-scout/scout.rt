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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Convenience field template to present {@link IDataModelAttribute#getOperators()}
 * <p>
 * Uses the lookup call {@link DataModelOperatorLookupCall}
 * <p>
 * Expects the property {@link #setAttribute(IDataModelAttribute)} to be set.
 */
public abstract class AbstractDataModelOperatorField extends AbstractSmartField<IDataModelAttributeOp> {

  public AbstractDataModelOperatorField() {
    this(true);
  }

  public AbstractDataModelOperatorField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected String getConfiguredLabel() {
    return ScoutTexts.get("Op");
  }

  @Override
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return DataModelOperatorLookupCall.class;
  }

  @Override
  protected void execAddSearchTerms(SearchFilter search) {
    //nop
  }

  public void setAttribute(IDataModelAttribute attribute) {
    IDataModelAttributeOp oldOp = getValue();
    ((DataModelOperatorLookupCall) getLookupCall()).setAttribute(attribute);
    IDataModelAttributeOp newOp = null;
    if (attribute != null) {
      setView(true, true, false);
      HashSet<IDataModelAttributeOp> tmp = new HashSet<IDataModelAttributeOp>();
      IDataModelAttributeOp[] ops = attribute.getOperators();
      tmp.addAll(Arrays.asList(ops));
      if (tmp.contains(oldOp)) {
        newOp = oldOp;
      }
      else if (ops.length > 0) {
        newOp = ops[0];
      }
    }
    else {
      setView(false, false, false);
    }
    setValue(newOp);
  }

  public IDataModelAttribute getAttribute() {
    return ((DataModelOperatorLookupCall) getLookupCall()).getAttribute();
  }

}
