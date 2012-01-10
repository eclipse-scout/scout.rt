/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.core.LogicalGridData;

public class RwtScoutFormFieldGridData extends LogicalGridData {
  private IFormField m_scoutField;

  public RwtScoutFormFieldGridData(IFormField scoutField) {
    m_scoutField = scoutField;
  }

  @Override
  public void validate() {
    GridData data = m_scoutField.getGridData();
    // setup constraints
    RwtScoutFormFieldGridData gc = this;
    gc.gridx = data.x;
    gc.gridy = data.y;
    gc.gridw = data.w;
    gc.gridh = data.h;
    gc.weightx = data.weightX;
    gc.weighty = data.weightY;
    if (gc.weightx < 0) {
      // inherit
      gc.weightx = Math.max(1.0, gc.gridw);
    }
    if (gc.weighty < 0) {
      // inherit
      gc.weighty = inheritWeightY(m_scoutField);
    }
    gc.useUiWidth = data.useUiWidth;
    gc.useUiHeight = data.useUiHeight;
    gc.horizontalAlignment = data.horizontalAlignment;
    gc.verticalAlignment = data.verticalAlignment;
    gc.fillHorizontal = data.fillHorizontal;
    gc.fillVertical = data.fillVertical;
    gc.widthHint = data.widthInPixel;
    gc.heightHint = data.heightInPixel;
    if (gc.weighty == 0 || gc.weighty < 0 && gc.gridh <= 1) {
      gc.fillVertical = false;
    }
  }

  private Double inheritWeightY(IFormField f) {
    Double d = inheritWeightYRec(f);
    if (d == null) {
      GridData data = f.getGridData();
      if (data.weightY >= 0) {
        d = data.weightY;
      }
      else {
        d = (double) (data.h >= 2 ? data.h : 0);
      }
    }
    return d;
  }

  private Double inheritWeightYRec(IFormField f) {
    boolean found = false;
    double sumWy = 0;
    if (f instanceof ICompositeField) {
      for (IFormField child : ((ICompositeField) f).getFields()) {
        if (child.isVisible()) {
          GridData data = child.getGridData();
          if (data.weightY < 0) {
            Double inheritWeightY = inheritWeightYRec(child);
            if (inheritWeightY != null) {
              found = true;
              sumWy += inheritWeightY;
            }
          }
          else {
            found = true;
            sumWy += data.weightY;
          }
        }
      }
    }
    else {
      sumWy = f.getGridData().h >= 2 ? f.getGridData().h : 0;
      found = true;
    }
    if (found) {
      return sumWy;
    }
    else {
      return null;
    }
  }

  @Override
  public String toString() {
    return m_scoutField.getLabel() + ": " + super.toString();
  }

}
