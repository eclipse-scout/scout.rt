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
package org.eclipse.scout.rt.ui.swt.form.fields.sequencebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFormFieldGridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>SwtScoutRangeBox</h3> ...
 * 
 * @since 1.0.0 15.04.2008
 */
public class SwtScoutSequenceBox extends SwtScoutFieldComposite<ISequenceBox> implements ISwtScoutSequenceBox {

  private PropertyChangeListener m_scoutMandatoryChangeListener;
  private PropertyChangeListener m_scoutErrorStatusChangeListener;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);

    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);

    Composite fieldContainer = getEnvironment().getFormToolkit().createComposite(container);
    for (IFormField scoutField : getScoutObject().getFields()) {
      ISwtScoutFormField swtFormField = getEnvironment().createFormField(fieldContainer, scoutField);
      // remove label fixed width hint
      ILabelComposite childLabel = swtFormField.getSwtLabel();
      if (childLabel != null && childLabel.getLayoutData() instanceof LogicalGridData) {
        ((LogicalGridData) childLabel.getLayoutData()).widthHint = 0;
      }
      // create layout constraints
      SwtScoutFormFieldGridData data = new SwtScoutFormFieldGridData(scoutField) {
        @Override
        public void validate() {
          super.validate();
          useUiWidth = !getScoutObject().isEqualColumnWidths();
          useUiHeight = true;
        }
      };
      swtFormField.getSwtContainer().setLayoutData(data);

    }
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(fieldContainer);

    // layout
    fieldContainer.setLayout(new LogicalGridLayout(6, 0));
    container.setLayout(new LogicalGridLayout(1, 0));
    setChildMandatoryFromScout();
  }

  @Override
  public Composite getSwtField() {
    return (Composite) super.getSwtField();
  }

  @Override
  public ISequenceBox getScoutObject() {
    return super.getScoutObject();
  }

  protected void setChildMandatoryFromScout() {
    boolean inheritedMandatory = false;
    for (IFormField f : getScoutObject().getFields()) {
      if (f.isVisible()) {
        if (f instanceof IValueField) {
          inheritedMandatory = ((IValueField) f).isMandatory();
          if (inheritedMandatory == true) {
            break;
          }
        }
      }
    }
    setMandatoryFromScout(inheritedMandatory);
  }

  protected void setChildErrorStatusFromScout() {
    IProcessingStatus inheritedErrorStatus = null;
    for (IFormField f : getScoutObject().getFields()) {
      if (f.isVisible()) {
        if (f instanceof IValueField) {
          inheritedErrorStatus = ((IValueField) f).getErrorStatus();
          // bsh 2010-10-01: always break (don't inherit error status from other fields than the first visible field)
          break;
        }
      }
    }
    setErrorStatusFromScout(inheritedErrorStatus);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // add mandatory change listener on children to decorate my label same as any mandatory child
    m_scoutMandatoryChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        Runnable j = new Runnable() {
          @Override
          public void run() {
            if (getSwtContainer() != null && !getSwtContainer().isDisposed()) {
              setChildMandatoryFromScout();
            }
          }
        };
        getEnvironment().invokeSwtLater(j);
      }
    };
    for (IFormField f : getScoutObject().getFields()) {
      f.addPropertyChangeListener(IFormField.PROP_MANDATORY, m_scoutMandatoryChangeListener);
    }
    // add errror status change listener on children to decorate my label same as any child with an error status
    m_scoutErrorStatusChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        Runnable j = new Runnable() {
          @Override
          public void run() {
            setChildErrorStatusFromScout();
          }
        };
        getEnvironment().invokeSwtLater(j);
      }
    };
    for (IFormField f : getScoutObject().getFields()) {
      f.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, m_scoutErrorStatusChangeListener);
    }

  }

  @Override
  protected void detachScout() {
    if (m_scoutMandatoryChangeListener != null) {
      for (IFormField f : getScoutObject().getFields()) {
        f.removePropertyChangeListener(IFormField.PROP_MANDATORY, m_scoutMandatoryChangeListener);
      }
      m_scoutMandatoryChangeListener = null;
    }
    super.detachScout();
  }
}
