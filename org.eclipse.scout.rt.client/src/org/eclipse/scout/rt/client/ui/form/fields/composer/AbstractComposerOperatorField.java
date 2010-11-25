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

import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.IComposerOp;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Convenience field template to present {@link IComposerAttribute#getOperators()}
 * <p>
 * Uses the lookup call {@link ComposerOperatorLookupCall}
 * <p>
 * Expects the property {@link #setComposerAttribute(IComposerAttribute)} to be set.
 */
public abstract class AbstractComposerOperatorField extends AbstractSmartField<IComposerOp> {

  @Override
  protected String getConfiguredLabel() {
    return ScoutTexts.get("Op");
  }

  @Override
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return ComposerOperatorLookupCall.class;
  }

  @Override
  protected void execAddSearchTerms(SearchFilter search) {
    //nop
  }

  public void setComposerAttribute(IComposerAttribute attribute) {
    IComposerOp oldOp = getValue();
    ((ComposerOperatorLookupCall) getLookupCall()).setComposerAttribute(attribute);
    IComposerOp newOp = null;
    if (attribute != null) {
      setView(true, true, false);
      HashSet<IComposerOp> tmp = new HashSet<IComposerOp>();
      IComposerOp[] ops = attribute.getOperators();
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

  public IComposerAttribute getComposerAttribute() {
    return ((ComposerOperatorLookupCall) getLookupCall()).getComposerAttribute();
  }

}
