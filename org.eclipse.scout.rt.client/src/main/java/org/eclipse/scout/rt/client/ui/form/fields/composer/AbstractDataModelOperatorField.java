/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

/**
 * Convenience field template to present {@link IDataModelAttribute#getOperators()}
 * <p>
 * Uses the lookup call {@link DataModelOperatorLookupCall}
 * <p>
 * Expects the property {@link #setAttribute(IDataModelAttribute)} to be set.
 */
@ClassId("46d6ba4b-07a6-4fd8-bf84-7e372e0f80bc")
public abstract class AbstractDataModelOperatorField extends AbstractSmartField<IDataModelAttributeOp> {

  public AbstractDataModelOperatorField() {
    this(true);
  }

  public AbstractDataModelOperatorField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected String getConfiguredLabel() {
    return TEXTS.get("Op");
  }

  @Override
  protected Class<? extends ILookupCall<IDataModelAttributeOp>> getConfiguredLookupCall() {
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
      Set<IDataModelAttributeOp> tmp = new HashSet<>();
      List<IDataModelAttributeOp> ops = attribute.getOperators();
      tmp.addAll(ops);
      if (tmp.contains(oldOp)) {
        newOp = oldOp;
      }
      else {
        newOp = CollectionUtility.firstElement(ops);
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
