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
package org.eclipse.scout.rt.ui.rap.form.fields.sequencebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.core.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.core.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.rap.form.fields.checkbox.IRwtScoutCheckbox;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutSequenceBox extends RwtScoutFieldComposite<ISequenceBox> implements IRwtScoutSequenceBox {

  private PropertyChangeListener m_scoutMandatoryChangeListener;
  private PropertyChangeListener m_scoutErrorStatusChangeListener;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Composite fieldContainer = getUiEnvironment().getFormToolkit().createComposite(container);
    int visibleCount = 0;
    for (IFormField scoutField : getScoutObject().getFields()) {
      IRwtScoutFormField childFormField = getUiEnvironment().createFormField(fieldContainer, scoutField);

      ILabelComposite childLabel = null;
      if (childFormField instanceof IRwtScoutCheckbox) {
        childLabel = ((IRwtScoutCheckbox) childFormField).getPlaceholderLabel();
      }
      else {
        childLabel = childFormField.getUiLabel();
      }
      if (childLabel != null && childLabel.getLayoutData() instanceof LogicalGridData) {
        //Make the label as small as possible
        ((LogicalGridData) childLabel.getLayoutData()).widthHint = 0;

        //Remove the label completely if the formField doesn't actually have a label on the left side
        if (removeLabelCompletely(childFormField)) {
          childLabel.setVisible(false);
        }
        // <bsh 2010-10-01>
        // Force an empty, but visible label for all but the first visible fields
        // within an SequenceBox. This empty status label is necessary to show the
        // "mandatory" indicator.
        if (childFormField.getScoutObject() instanceof IFormField) {
          IFormField childScoutField = ((IFormField) childFormField.getScoutObject());
          if (childScoutField.isVisible()) {
            visibleCount++;
          }
          if (visibleCount > 1) {
            if (!childLabel.getVisible()) {
              // make the label visible, but clear any text
              childLabel.setVisible(true);
              childLabel.setText("");
            }
          }
        }
        // <bsh>
      }
      // create layout constraints
      RwtScoutFormFieldGridData data = new RwtScoutFormFieldGridData(scoutField) {
        @Override
        public void validate() {
          super.validate();
          useUiWidth = !getScoutObject().isEqualColumnWidths();
          useUiHeight = true;
        }
      };
      childFormField.getUiContainer().setLayoutData(data);

    }
    //
    setUiContainer(container);
    setUiLabel(label);
    setUiField(fieldContainer);

    // layout
    fieldContainer.setLayout(new LogicalGridLayout(6, 0));
    container.setLayout(new LogicalGridLayout(1, 0));
    setChildMandatoryFromScout();
  }

  private boolean removeLabelCompletely(IRwtScoutFormField rwtScoutFormField) {
    if (rwtScoutFormField == null) {
      return false;
    }

    if (!(rwtScoutFormField.getScoutObject() instanceof IFormField)) {
      return false;
    }

    IFormField formField = ((IFormField) rwtScoutFormField.getScoutObject());
    if (formField.getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
      return true;
    }

    if (formField instanceof IBooleanField) {
      return true;
    }

    return false;
  }

  @Override
  public Composite getUiField() {
    return (Composite) super.getUiField();
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
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        Runnable j = new Runnable() {
          @Override
          public void run() {
            if (getUiContainer() != null && !getUiContainer().isDisposed()) {
              setChildMandatoryFromScout();
            }
          }
        };
        getUiEnvironment().invokeUiLater(j);
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
        getUiEnvironment().invokeUiLater(j);
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
