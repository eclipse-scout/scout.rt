/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

@ClassId("d9667ad1-93a3-4675-a158-c1da9931c7df")
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

  public void setSelectedValues(List<?> o) {
    setSelectedValuesInternal(o);
    // single observer
    activateValueField();
  }

  private void setSelectedValuesInternal(List<?> values0) {
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
    return TEXTS.get("ExtendedSearchAddAttribute");
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
  @ClassId("1e682698-544a-4636-9332-4a3e0405f257")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(1)
    @ClassId("1f790491-1a71-4252-9209-e9277dbd77d5")
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

      @Order(1)
      @ClassId("4d2fc7d9-186e-4ea6-bce8-cb35c1f3b35b")
      public class AttributeField extends AbstractListBox<IDataModelAttribute> {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Attribute");
        }

        @Override
        protected List<ILookupRow<IDataModelAttribute>> execLoadTableData() {
          List<IDataModelAttribute> a = getAvailableAttributes();
          List<ILookupRow<IDataModelAttribute>> result = new ArrayList<>(a.size());
          for (IDataModelAttribute attribute : a) {
            if (attribute.isVisible()) {
              result.add(new LookupRow<>(attribute, attribute.getText()).withIconId(attribute.getIconId()));
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
          catch (RuntimeException | PlatformError e) {
            BEANS.get(ExceptionHandler.class).handle(e);
          }
          // activate corresponding data field
          activateValueField();
        }
      }

      @Order(2)
      @ClassId("dd082fb2-2f03-41df-be38-ffa9811fa614")
      public class OperatorField extends AbstractListBox<IDataModelAttributeOp> {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Op");
        }

        @Override
        protected List<ILookupRow<IDataModelAttributeOp>> execLoadTableData() {
          List<IDataModelAttributeOp> ops = null;
          IDataModelAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            ops = att.getOperators();
          }
          if (ops != null) {
            List<ILookupRow<IDataModelAttributeOp>> result = new ArrayList<>(ops.size());
            for (IDataModelAttributeOp op : ops) {
              String text = op.getText();
              if (text != null && text.contains("{0}")) {
                text = text.replace("{0}", "n");
              }
              if (text != null && text.contains("{1}")) {
                text = text.replace("{1}", "m");
              }
              result.add(new LookupRow<>(op, text));
            }
            return result;
          }
          return new ArrayList<>(0);
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
      @ClassId("6fd21045-eee6-41fd-a8d6-ef1cfd40209a")
      public class ValueField extends AbstractComposerValueBox {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Value");
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
    @ClassId("1b3d5630-209e-492e-8d0e-1f1ab7672730")
    public class OkButton extends AbstractOkButton {
    }

    @Order(101)
    @ClassId("83c8bafb-ca6a-49e9-9e8e-49414bfb1511")
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
  }

  public void startNew() {
    startInternal(new NewHandler());
  }

  public void startModify() {
    startInternal(new ModifyHandler());
  }

}
