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
package org.eclipse.scout.rt.ui.swing.form.fields.rangebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFormFieldGridData;

/**
 * A group box is a composite of the following structure: groupBox bodyPart
 * processButtonPart systemProcessButtonPart customProcessButtonPart
 */
public class SwingScoutSequenceBox extends SwingScoutFieldComposite<ISequenceBox> implements ISwingScoutSequenceBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutSequenceBox.class);

  private PropertyChangeListener m_scoutMandatoryChangeListener;
  private PropertyChangeListener m_scoutErrorStatusChangeListener;

  @Override
  protected void initializeSwing() {
    // swing layout
    JPanelEx container = new JPanelEx();
    container.setName(getScoutObject().getClass().getName() + ".container");
    //
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    //
    JPanelEx innerFieldsContainer = new JPanelEx();
    container.add(innerFieldsContainer);
    //
    // add fields
    IFormField[] scoutFields = getScoutObject().getFields();
    int visibleCount = 0;
    for (int i = 0; i < scoutFields.length; i++) {
      // create item
      ISwingScoutFormField swingScoutComposite = getSwingEnvironment().createFormField(innerFieldsContainer, scoutFields[i]);
      // remove label fixed width hint
      JStatusLabelEx childLabel = swingScoutComposite.getSwingLabel();
      if (childLabel != null) {
        childLabel.setFixedSize(0);
        // <bsh 2010-10-01>
        // Force an empty, but visible label for all but the first visible fields
        // within an SequenceBox. This empty status label is necessary to show the
        // "mandatory" indicator.
        if (swingScoutComposite.getScoutObject() instanceof IFormField) {
          IFormField myScoutObject = ((IFormField) swingScoutComposite.getScoutObject());
          if (myScoutObject.isVisible()) {
            visibleCount++;
          }
          if (visibleCount > 1) {
            if (!childLabel.isVisible()) {
              // make the label visible, but clear any text
              childLabel.setVisible(true);
              childLabel.setText("");
            }
          }
        }
        // <bsh>
      }
      // create layout constraints
      SwingScoutFormFieldGridData data = new SwingScoutFormFieldGridData(scoutFields[i]) {
        @Override
        public void validate() {
          super.validate();
          useUiWidth = (!getScoutObject().isEqualColumnWidths());
          useUiHeight = true;
        }
      };
      swingScoutComposite.getSwingContainer().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, data);
      innerFieldsContainer.add(swingScoutComposite.getSwingContainer());
    }
    setSwingLabel(label);
    setSwingField(innerFieldsContainer);
    setSwingContainer(container);
    innerFieldsContainer.setLayout(new LogicalGridLayout(getSwingEnvironment(), getSwingEnvironment().getFormColumnGap(), 0));
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
    setChildMandatoryFromScout();
  }

  @Override
  public JPanel getSwingRangeBox() {
    return (JPanel) getSwingField();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    getSwingRangeBox().setEnabled(b);
  }

  protected void setChildMandatoryFromScout() {
    boolean inheritedMandatory = false;
    for (IFormField f : getScoutObject().getFields()) {
      if (f.isVisible()) {
        if (f instanceof IValueField) {
          inheritedMandatory = ((IValueField) f).isMandatory();
          // bsh 2010-10-01: always break (don't inherit mandatory flag from other fields than the first visible field)
          // Old code: if (inheritedMandatory == true) { break; }
          break;
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
            setChildMandatoryFromScout();
          }
        };
        getSwingEnvironment().invokeSwingLater(j);
      }
    };
    for (IFormField f : getScoutObject().getFields()) {
      f.addPropertyChangeListener(IFormField.PROP_MANDATORY, m_scoutMandatoryChangeListener);
      f.addPropertyChangeListener(IFormField.PROP_VISIBLE, m_scoutMandatoryChangeListener); // bsh 2010-10-01: update mandatory status when the first field changes its visibility
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
        getSwingEnvironment().invokeSwingLater(j);
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
        f.removePropertyChangeListener(IFormField.PROP_VISIBLE, m_scoutMandatoryChangeListener);
      }
      m_scoutMandatoryChangeListener = null;
    }
    if (m_scoutErrorStatusChangeListener != null) {
      for (IFormField f : getScoutObject().getFields()) {
        f.removePropertyChangeListener(IFormField.PROP_ERROR_STATUS, m_scoutErrorStatusChangeListener);
      }
      m_scoutErrorStatusChangeListener = null;
    }
    super.detachScout();
  }
}
