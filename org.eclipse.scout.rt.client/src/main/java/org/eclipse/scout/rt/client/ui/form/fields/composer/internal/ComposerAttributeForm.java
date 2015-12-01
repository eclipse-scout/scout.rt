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
import java.util.Collection;
import java.util.List;

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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class ComposerAttributeForm extends AbstractForm {
  private List<IDataModelAttribute> m_validAttributes;
  /**
   * result values
   */
  private List<Object> m_selectedValues;
  /**
   * result display values
   */
  private List<String> m_selectedDisplayValues;

  public ComposerAttributeForm() {
    super();
  }

  /**
   * form property
   */
  public List<IDataModelAttribute> getAvailableAttributes() {
    return CollectionUtility.arrayList(m_validAttributes);
  }

  public void setAvailableAttributes(List<? extends IDataModelAttribute> attributes0) {
    m_validAttributes = CollectionUtility.arrayListWithoutNullElements(attributes0);
    // single observer, reload attributes listbox
    getAttributeField().loadListBoxData();
  }

  /**
   * form properties
   */
  public List<Object> getSelectedValues() {
    return CollectionUtility.arrayList(m_selectedValues);
  }

  public void setSelectedValues(List<? extends Object> o) {
    setSelectedValuesInternal(o);
    // single observer
    activateValueField();
  }

  private void setSelectedValuesInternal(List<? extends Object> values0) {
    m_selectedValues = CollectionUtility.arrayListWithoutNullElements(values0);
  }

  public List<String> getSelectedDisplayValues() {
    return CollectionUtility.arrayList(m_selectedDisplayValues);
  }

  public void setSelectedDisplayValues(List<String> s) {
    setSelectedDisplayValuesInternal(s);
  }

  private void setSelectedDisplayValuesInternal(List<String> displayValues0) {
    m_selectedDisplayValues = CollectionUtility.arrayListWithoutNullElements(displayValues0);
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
  protected String getConfiguredTitle() {
    return ScoutTexts.get("ExtendedSearchAddAttribute");
  }

  /**
   * activate value field function
   */
  private void activateValueField() {
    IDataModelAttribute att = getAttributeField().getCheckedKey();
    IDataModelAttributeOp op = getOperatorField().getCheckedKey();
    List<Object> newValues = getSelectedValues();
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
        protected List<ILookupRow<IDataModelAttribute>> execLoadTableData() {
          List<IDataModelAttribute> a = getAvailableAttributes();
          List<ILookupRow<IDataModelAttribute>> result = new ArrayList<ILookupRow<IDataModelAttribute>>(a.size());
          for (IDataModelAttribute attribute : a) {
            if (attribute.isVisible()) {
              result.add(new LookupRow<IDataModelAttribute>(attribute, attribute.getText()).withIconId(attribute.getIconId()));
            }
          }
          return result;
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
        protected void execInitField() {
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
              Collection<IDataModelAttributeOp> ops = getOperatorField().getValue();
              if (CollectionUtility.hasElements(ops)) {
                getOperatorField().checkKey(CollectionUtility.firstElement(ops));
              }
            }
          }
          catch (RuntimeException e) {
            BEANS.get(ExceptionHandler.class).handle(e);
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
        protected List<ILookupRow<IDataModelAttributeOp>> execLoadTableData() {
          List<IDataModelAttributeOp> ops = null;
          IDataModelAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            ops = att.getOperators();
          }
          if (ops != null) {
            List<ILookupRow<IDataModelAttributeOp>> result = new ArrayList<ILookupRow<IDataModelAttributeOp>>(ops.size());
            for (IDataModelAttributeOp op : ops) {
              String text = op.getText();
              if (text != null && text.indexOf("{0}") >= 0) {
                text = text.replace("{0}", "n");
              }
              if (text != null && text.indexOf("{1}") >= 0) {
                text = text.replace("{1}", "m");
              }
              result.add(new LookupRow<IDataModelAttributeOp>(op, text));
            }
            return result;
          }
          return new ArrayList<ILookupRow<IDataModelAttributeOp>>(0);
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
        protected void execInitField() {
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
        protected void execChangedValue() {
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
    protected void execLoad() {
      getAttributeField().getTable().selectFirstRow();
      getOperatorField().getTable().selectFirstRow();
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
    @Override
    protected void execLoad() {
    }
  }

  public void startNew() {
    startInternal(new NewHandler());
  }

  public void startModify() {
    startInternal(new ModifyHandler());
  }

}
