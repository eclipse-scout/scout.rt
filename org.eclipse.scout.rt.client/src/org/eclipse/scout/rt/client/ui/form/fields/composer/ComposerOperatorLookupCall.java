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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.IComposerOp;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Convenience lookup call to present {@link IComposerAttribute#getOperators()}
 * <p>
 * This lookup call expects the property {@link #setComposerAttribute(IComposerAttribute)} to be set.
 */
public class ComposerOperatorLookupCall extends LocalLookupCall {
  private static final long serialVersionUID = 1L;

  private IComposerAttribute m_attribute;

  public void setComposerAttribute(IComposerAttribute attribute) {
    m_attribute = attribute;
  }

  public IComposerAttribute getComposerAttribute() {
    return m_attribute;
  }

  @Override
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    List<LookupRow> result = new ArrayList<LookupRow>();
    IComposerOp[] ops = null;
    if (m_attribute != null) {
      ops = m_attribute.getOperators();
    }
    if (ops != null) {
      for (IComposerOp op : ops) {
        String text = op.getShortText();
        if (text != null && text.indexOf("{0}") >= 0) {
          text = text.replace("{0}", "n");
        }
        if (text != null && text.indexOf("{1}") >= 0) {
          text = text.replace("{1}", "m");
        }
        result.add(new LookupRow(op, text));
      }
    }

    return result;
  }
}
