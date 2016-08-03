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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFormFieldGridData;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutSequenceBox extends RwtScoutFieldComposite<ISequenceBox> implements IRwtScoutSequenceBox {

  private PropertyChangeListener m_changeListener;

  private Map<IFormField, IRwtScoutFormField> m_fieldMap = new HashMap<IFormField, IRwtScoutFormField>();

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Composite fieldContainer = getUiEnvironment().getFormToolkit().createComposite(container);
    for (IFormField scoutField : getScoutObject().getFields()) {
      IRwtScoutFormField childFormField = getUiEnvironment().createFormField(fieldContainer, scoutField);
      m_fieldMap.put(scoutField, childFormField);

      ILabelComposite childLabel = childFormField.getUiLabel();
      if (childLabel != null) {
        childLabel.setGrabHorizontalEnabled(false); // disable grabbing so that mandatory and error status are rendered as expected, and the label is not swallowed.
        if (childLabel.getLayoutData() instanceof LogicalGridData) {
          ((LogicalGridData) childLabel.getLayoutData()).widthHint = 0; // Make the label as small as possible
        }
      }

      // create layout constraints
      final boolean checkbox = scoutField instanceof IBooleanField;
      RwtScoutFormFieldGridData data = new RwtScoutFormFieldGridData(scoutField) {
        @Override
        public void validate() {
          super.validate();
          useUiWidth = !getScoutObject().isEqualColumnWidths() || checkbox;
          useUiHeight = true;
          weightx = (checkbox ? 0 : weightx); // to enforce the checkbox to be rendered completely (with its label)
        }
      };
      childFormField.getUiContainer().setLayoutData(data);
    }

    //
    setUiContainer(container);
    setUiLabel(label);
    setUiField(fieldContainer);

    // layout
    fieldContainer.setLayout(new LogicalGridLayout(getGridColumnGapInPixel(), 0));
    container.setLayout(new LogicalGridLayout(1, 0));

    applyInheritedDecoration();
  }

  protected int getGridColumnGapInPixel() {
    return 6;
  }

  private boolean removeLabelCompletely(IRwtScoutFormField rwtScoutFormField) {
    if (rwtScoutFormField == null) {
      return false;
    }

    if (!(rwtScoutFormField.getScoutObject() instanceof IFormField)) {
      return false;
    }

    IFormField formField = ((IFormField) rwtScoutFormField.getScoutObject());
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

  /**
   * Applies the mandatory and error status of the first visible field to the sequence box.
   */
  public void applyInheritedDecoration() {
    // Make the status label part visible for all fields.
    for (IFormField field : getScoutObject().getFields()) {
      ILabelComposite label = m_fieldMap.get(field).getUiLabel();
      if (label != null) {
        m_fieldMap.get(field).getUiLabel().setStatusVisible(field.isMandatory() || field.getErrorStatus() != null);
      }
    }

    // Apply 'mandatory' or 'error' status of the first visible field to the sequence box.
    boolean inheritedMandatory = false;
    IProcessingStatus inheritedErrorStatus = null;

    for (IFormField candidate : getScoutObject().getFields()) {
      if (candidate.isVisible() && m_fieldMap.get(candidate).getUiLabel() != null) {
        // This is the first visible field of the sequence box. Hide the status label.
        m_fieldMap.get(candidate).getUiLabel().setStatusVisible(false);

        inheritedMandatory = candidate.isMandatory();
        inheritedErrorStatus = candidate.getErrorStatus();
        break;
      }
    }

    setMandatoryFromScout(inheritedMandatory);
    setErrorStatusFromScout(inheritedErrorStatus);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // Only change color for the label, the field container should not reflect enabled / disabled state. The child fields handle the state independently.
    if (getUiLabel() != null) {
      if (getUiLabel().getEnabled() != b) {
        getUiLabel().setEnabled(b);
      }
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    // add mandatory change listener on children to decorate my label same as any mandatory child
    m_changeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        Runnable j = new Runnable() {
          @Override
          public void run() {
            applyInheritedDecoration();
          }
        };
        getUiEnvironment().invokeUiLater(j);
      }
    };
    for (IFormField f : getScoutObject().getFields()) {
      f.addPropertyChangeListener(IFormField.PROP_VISIBLE, m_changeListener);
      f.addPropertyChangeListener(IFormField.PROP_LABEL_VISIBLE, m_changeListener);
      f.addPropertyChangeListener(IFormField.PROP_LABEL, m_changeListener);
      f.addPropertyChangeListener(IFormField.PROP_MANDATORY, m_changeListener);
      f.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, m_changeListener);
    }
  }

  @Override
  protected void detachScout() {
    if (m_changeListener != null) {
      for (IFormField f : getScoutObject().getFields()) {
        f.removePropertyChangeListener(IFormField.PROP_VISIBLE, m_changeListener);
        f.removePropertyChangeListener(IFormField.PROP_LABEL_VISIBLE, m_changeListener);
        f.removePropertyChangeListener(IFormField.PROP_LABEL, m_changeListener);
        f.removePropertyChangeListener(IFormField.PROP_MANDATORY, m_changeListener);
        f.removePropertyChangeListener(IFormField.PROP_ERROR_STATUS, m_changeListener);
      }
      m_changeListener = null;
    }
    super.detachScout();
  }

  @Override
  protected void updateKeyStrokesFromScout() {
    // nop because the child fields also register the keystrokes of theirs parents
  }
}
