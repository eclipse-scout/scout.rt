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
package org.eclipse.scout.rt.client.ui.form.fields.composer.internal;

import java.util.ArrayList;

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerValueField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.AttributeField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.OperatorField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.ValueField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public class ComposerAttributeForm extends AbstractForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ComposerAttributeForm.class);

  private IDataModelAttribute[] m_validAttributes;
  /**
   * result values
   */
  private Object[] m_selectedValues;
  /**
   * result display values
   */
  private String[] m_selectedDisplayValues;

  public ComposerAttributeForm() throws ProcessingException {
    super();
  }

  /**
   * form property
   */
  public IDataModelAttribute[] getAvailableAttributes() {
    return m_validAttributes;
  }

  public void setAvailableAttributes(IDataModelAttribute[] a) throws ProcessingException {
    m_validAttributes = a;
    // single observer, reload attributes listbox
    getAttributeField().loadListBoxData();
  }

  /**
   * form properties
   */
  public Object[] getSelectedValues() {
    return m_selectedValues;
  }

  public void setSelectedValues(Object[] o) {
    setSelectedValuesInternal(o);
    // single observer
    activateValueField();
  }

  private void setSelectedValuesInternal(Object[] o) {
    m_selectedValues = o;
  }

  public String[] getSelectedDisplayValues() {
    return m_selectedDisplayValues;
  }

  public void setSelectedDisplayValues(String[] s) {
    setSelectedDisplayValuesInternal(s);
  }

  private void setSelectedDisplayValuesInternal(String[] s) {
    m_selectedDisplayValues = s;
  }

  public IDataModelAttribute getSelectedAttribute() {
    return getAttributeField().getCheckedKey();
  }

  public void setSelectedAttribute(IDataModelAttribute a) {
    getAttributeField().checkKey(a);
  }

  public IDataModelAttributeOp getSelectedOp() {
    return getOperatorField().getCheckedKey();
  }

  public void setSelectedOp(IDataModelAttributeOp op) {
    getOperatorField().checkKey(op);
  }

  @Override
  @ConfigPropertyValue("\"ExtendedSearchAddAttribute\"")
  protected String getConfiguredTitle() {
    return ScoutTexts.get("ExtendedSearchAddAttribute");
  }

  /**
   * activate value field function
   */
  private void activateValueField() {
    IDataModelAttribute att = getAttributeField().getCheckedKey();
    IDataModelAttributeOp op = getOperatorField().getCheckedKey();
    Object[] newValues = getSelectedValues();
    //
    if (att == null) {
      getValueField().clearSelectionContext();
    }
    else {
      getValueField().setSelectionContext(att, op, newValues);
    }
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public AttributeField getAttributeField() {
    return getRootGroupBox().getFieldByClass(AttributeField.class);
  }

  public OperatorField getOperatorField() {
    return getRootGroupBox().getFieldByClass(OperatorField.class);
  }

  public ValueField getValueField() {
    return getRootGroupBox().getFieldByClass(ValueField.class);
  }

  public OkButton getOkButton() {
    return getRootGroupBox().getFieldByClass(OkButton.class);
  }

  public CancelButton getCancelButton() {
    return getRootGroupBox().getFieldByClass(CancelButton.class);
  }

  @Order(1)
  public class MainBox extends AbstractGroupBox {

    @Override
    @ConfigPropertyValue("2")
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(1)
    public class SequenceBox extends AbstractSequenceBox {

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridW() {
        return 2;
      }

      @Override
      protected int getConfiguredGridH() {
        return 12;
      }

      @Override
      protected boolean getConfiguredAutoCheckFromTo() {
        return false;
      }

      @Override
      protected boolean getConfiguredEqualColumnWidths() {
        return true;
      }

      @Order(1)
      public class AttributeField extends AbstractListBox<IDataModelAttribute> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Attribute");
        }

        @Override
        protected String getConfiguredIconId() {
          return AbstractIcons.ComposerFieldAttribute;
        }

        @Override
        protected LookupRow[] execLoadTableData() throws ProcessingException {
          IDataModelAttribute[] a = getAvailableAttributes();
          ArrayList<LookupRow> list = new ArrayList<LookupRow>();
          if (a != null) {
            for (int i = 0; i < a.length; i++) {
              if (a[i].isVisible()) {
                list.add(new LookupRow(a[i], a[i].getText(), a[i].getIconId()));
              }
            }
          }
          LookupRow[] rows = new LookupRow[list.size()];
          return list.toArray(rows);
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridH() {
          return 12;
        }

        @Override
        protected void execInitField() throws ProcessingException {
          getTable().setMultiCheck(false);
        }

        @Override
        protected void execChangedValue() {
          // change operator set
          try {
            IDataModelAttributeOp oldOp = getOperatorField().getCheckedKey();
            getOperatorField().loadListBoxData();
            getOperatorField().checkKey(oldOp);
            if (getOperatorField().getCheckedKey() == null) {
              IDataModelAttributeOp[] ops = getOperatorField().getValue();
              if (ops != null && ops.length > 0) {
                getOperatorField().checkKey(ops[0]);
              }
            }
          }
          catch (ProcessingException e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e);
          }
          // activate corresponding data field
          activateValueField();
        }
      }

      @Order(2)
      public class OperatorField extends AbstractListBox<IDataModelAttributeOp> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Op");
        }

        @Override
        protected LookupRow[] execLoadTableData() throws ProcessingException {
          IDataModelAttributeOp[] ops = null;
          IDataModelAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            ops = att.getOperators();
          }
          LookupRow[] rows = null;
          if (ops != null) {
            rows = new LookupRow[ops.length];
            for (int i = 0; i < rows.length; i++) {
              IDataModelAttributeOp id = ops[i];
              String text = ops[i].getText();
              if (text != null && text.indexOf("{0}") >= 0) {
                text = text.replace("{0}", "n");
              }
              if (text != null && text.indexOf("{1}") >= 0) {
                text = text.replace("{1}", "m");
              }
              rows[i] = new LookupRow(id, text);
            }
          }
          return rows;
        }

        @Override
        protected void execChangedValue() {
          // activate corresponding data field
          activateValueField();
        }

        @Override
        protected String getConfiguredIconId() {
          return null;
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridH() {
          return 12;
        }

        @Override
        protected void execInitField() throws ProcessingException {
          getTable().setMultiCheck(false);
        }
      }

      @Order(3)
      public class ValueField extends AbstractComposerValueBox {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridH() {
          return 12;
        }

        @Override
        protected void execChangedValue() throws ProcessingException {
          IComposerValueField f = getSelectedField();
          if (f == null) {
            setSelectedValuesInternal(null);
            setSelectedDisplayValuesInternal(null);
          }
          else {
            setSelectedValuesInternal(f.getValues());
            setSelectedDisplayValuesInternal(f.getTexts());
          }
        }
      }
    }

    @Order(100)
    public class OkButton extends AbstractOkButton {
    }

    @Order(101)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {
    @Override
    protected void execLoad() throws ProcessingException {
      getAttributeField().getTable().selectFirstRow();
      getOperatorField().getTable().selectFirstRow();
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
    @Override
    protected void execLoad() throws ProcessingException {
    }
  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

}
