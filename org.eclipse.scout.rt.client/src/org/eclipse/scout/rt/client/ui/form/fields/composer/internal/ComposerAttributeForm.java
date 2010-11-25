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
import java.util.HashMap;

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.AttributeField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.DateField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.DateTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.DoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.DummyField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.IntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.ListBoxField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.LongField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.OperatorField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.PercentField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.PlainDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.PlainIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.PlainLongField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.SmartField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.StringField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.TimeField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerAttributeForm.MainBox.SequenceBox.TreeBoxField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.IComposerOp;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.timefield.AbstractTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public class ComposerAttributeForm extends AbstractForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ComposerAttributeForm.class);

  private IComposerAttribute[] m_validAttributes;
  /**
   * result value
   */
  private Object m_selectedValue;
  /**
   * result display value
   */
  private String m_selectedDisplayValue;

  public ComposerAttributeForm() throws ProcessingException {
    super();
  }

  /**
   * form property
   */
  public IComposerAttribute[] getAvailableAttributes() {
    return m_validAttributes;
  }

  public void setAvailableAttributes(IComposerAttribute[] a) throws ProcessingException {
    m_validAttributes = a;
    // single observer, reload attributes listbox
    getAttributeField().loadListBoxData();
  }

  /**
   * form properties
   */
  public Object getSelectedValue() {
    return m_selectedValue;
  }

  public void setSelectedValue(Object o) {
    setSelectedValueInternal(o);
    // single observer
    activateValueField();
  }

  private void setSelectedValueInternal(Object o) {
    m_selectedValue = o;
  }

  public String getSelectedDisplayValue() {
    return m_selectedDisplayValue;
  }

  public void setSelectedDisplayValue(String s) {
    setSelectedDisplayValueInternal(s);
  }

  private void setSelectedDisplayValueInternal(String s) {
    m_selectedDisplayValue = s;
  }

  public IComposerAttribute getSelectedAttribute() {
    return getAttributeField().getCheckedKey();
  }

  public void setSelectedAttribute(IComposerAttribute a) {
    getAttributeField().checkKey(a);
  }

  public IComposerOp getSelectedOp() {
    return getOperatorField().getCheckedKey();
  }

  public void setSelectedOp(IComposerOp op) {
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
  @SuppressWarnings("unchecked")
  private void activateValueField() {
    IComposerAttribute att = getAttributeField().getCheckedKey();
    IComposerOp op = getOperatorField().getCheckedKey();
    HashMap<Integer, IValueField> map = new HashMap<Integer, IValueField>();
    map.put(IComposerAttribute.TYPE_DATE, getDateField());
    map.put(IComposerAttribute.TYPE_DATE_TIME, getDateTimeField());
    map.put(IComposerAttribute.TYPE_DOUBLE, getDoubleField());
    map.put(IComposerAttribute.TYPE_INTEGER, getIntegerField());
    map.put(IComposerAttribute.TYPE_AGGREGATE_COUNT, getLongField());
    map.put(IComposerAttribute.TYPE_NUMBER_LIST, getListBoxField());
    map.put(IComposerAttribute.TYPE_NUMBER_TREE, getTreeBoxField());
    map.put(IComposerAttribute.TYPE_CODE_LIST, getListBoxField());
    map.put(IComposerAttribute.TYPE_CODE_TREE, getTreeBoxField());
    map.put(IComposerAttribute.TYPE_LONG, getLongField());
    map.put(IComposerAttribute.TYPE_PERCENT, getPercentField());
    map.put(IComposerAttribute.TYPE_PLAIN_DOUBLE, getPlainDoubleField());
    map.put(IComposerAttribute.TYPE_PLAIN_INTEGER, getPlainIntegerField());
    map.put(IComposerAttribute.TYPE_PLAIN_LONG, getPlainLongField());
    map.put(IComposerAttribute.TYPE_STRING, getStringField());
    map.put(IComposerAttribute.TYPE_FULL_TEXT, getStringField());
    map.put(IComposerAttribute.TYPE_SMART, getSmartField());
    map.put(IComposerAttribute.TYPE_TIME, getTimeField());
    map.put(IComposerAttribute.TYPE_NONE, getDummyField());
    //
    int type = IComposerAttribute.TYPE_NONE;
    if (op != null) {
      type = op.getType();
    }
    if (type == IComposerAttribute.TYPE_INHERITED) {
      if (att != null) {
        type = att.getType();
      }
    }
    IValueField targetField = map.get(type);
    if (targetField == null) {
      targetField = getDummyField();
    }
    for (IValueField f : map.values()) {
      if (f == targetField) {
        if (f == getDummyField()) {
          f.setVisible(true);
          f.setMandatory(false);
        }
        else {
          Object o = getSelectedValue();
          Object singleValue = null;
          Object arrayValue = null;
          if (o != null && o.getClass().isArray()) {
            arrayValue = o;
          }
          else {
            singleValue = o;
          }
          //
          if (f instanceof IListBox) {
            LookupCall newCall = (att != null ? att.getLookupCall() : null);
            IListBox listBox = (IListBox) f;
            if (listBox.getLookupCall() != newCall) {
              listBox.setLookupCall(att != null ? att.getLookupCall() : null);
              try {
                listBox.loadListBoxData();
              }
              catch (Exception e) {
                LOG.warn(null, e);
                // nop
              }
            }
            try {
              f.setValue(arrayValue);
            }
            catch (Exception e) {
              // nop
            }
          }
          else if (f instanceof ITreeBox) {
            LookupCall newCall = (att != null ? att.getLookupCall() : null);
            ITreeBox treeBox = (ITreeBox) f;
            if (treeBox.getLookupCall() != newCall) {
              treeBox.setLookupCall(newCall);
              try {
                treeBox.loadRootNode();
                treeBox.getTree().setNodeExpanded(treeBox.getTree().getRootNode(), true);
              }
              catch (Exception e) {
                LOG.warn(null, e);
                // nop
              }
            }
            try {
              f.setValue(arrayValue);
            }
            catch (Exception e) {
              // nop
            }
          }
          else if (f instanceof ISmartField) {
            LookupCall newCall = (att != null ? att.getLookupCall() : null);
            ISmartField smartField = (ISmartField) f;
            if (smartField.getLookupCall() != newCall) {
              smartField.setLookupCall(newCall);
            }
            try {
              f.setValue(singleValue);
            }
            catch (Exception e) {
              // nop
            }
          }
          else {
            try {
              f.setValue(singleValue);
            }
            catch (Exception e) {
              // nop
            }
          }
          f.setMandatory(true);
          f.setVisible(true);
        }
      }
      else {
        f.setMandatory(false);
        f.setVisible(false);
      }
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

  public DateField getDateField() {
    return getRootGroupBox().getFieldByClass(DateField.class);
  }

  public DateTimeField getDateTimeField() {
    return getRootGroupBox().getFieldByClass(DateTimeField.class);
  }

  public DoubleField getDoubleField() {
    return getRootGroupBox().getFieldByClass(DoubleField.class);
  }

  public IntegerField getIntegerField() {
    return getRootGroupBox().getFieldByClass(IntegerField.class);
  }

  public ListBoxField getListBoxField() {
    return getRootGroupBox().getFieldByClass(ListBoxField.class);
  }

  public TreeBoxField getTreeBoxField() {
    return getRootGroupBox().getFieldByClass(TreeBoxField.class);
  }

  public LongField getLongField() {
    return getRootGroupBox().getFieldByClass(LongField.class);
  }

  public PercentField getPercentField() {
    return getRootGroupBox().getFieldByClass(PercentField.class);
  }

  public PlainDoubleField getPlainDoubleField() {
    return getRootGroupBox().getFieldByClass(PlainDoubleField.class);
  }

  public PlainIntegerField getPlainIntegerField() {
    return getRootGroupBox().getFieldByClass(PlainIntegerField.class);
  }

  public PlainLongField getPlainLongField() {
    return getRootGroupBox().getFieldByClass(PlainLongField.class);
  }

  public StringField getStringField() {
    return getRootGroupBox().getFieldByClass(StringField.class);
  }

  public SmartField getSmartField() {
    return getRootGroupBox().getFieldByClass(SmartField.class);
  }

  public DummyField getDummyField() {
    return getRootGroupBox().getFieldByClass(DummyField.class);
  }

  public TimeField getTimeField() {
    return getRootGroupBox().getFieldByClass(TimeField.class);
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
      public class AttributeField extends AbstractListBox<IComposerAttribute> {
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
          IComposerAttribute[] a = getAvailableAttributes();
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
            IComposerOp oldOp = getOperatorField().getCheckedKey();
            getOperatorField().loadListBoxData();
            getOperatorField().checkKey(oldOp);
            if (getOperatorField().getCheckedKey() == null) {
              IComposerOp[] ops = getOperatorField().getValue();
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
      public class OperatorField extends AbstractListBox<IComposerOp> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Op");
        }

        @Override
        protected LookupRow[] execLoadTableData() throws ProcessingException {
          IComposerOp[] ops = null;
          IComposerAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            ops = att.getOperators();
          }
          LookupRow[] rows = null;
          if (ops != null) {
            rows = new LookupRow[ops.length];
            for (int i = 0; i < rows.length; i++) {
              IComposerOp id = ops[i];
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
      public class ListBoxField extends AbstractListBox<Object> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridH() {
          return 12;
        }

        @Override
        protected boolean getConfiguredAutoLoad() {
          return false;
        }

        @Override
        protected void execPrepareLookup(LookupCall call) throws ProcessingException {
          IComposerAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            att.prepareLookup(call);
          }
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getCheckedKeys());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(3.5)
      public class TreeBoxField extends AbstractTreeBox<Object> {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridH() {
          return 12;
        }

        @Override
        protected boolean getConfiguredAutoLoad() {
          return false;
        }

        @Override
        protected void execPrepareLookup(LookupCall call, ITreeNode parent) throws ProcessingException {
          IComposerAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            att.prepareLookup(call);
          }
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getCheckedKeys());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(4)
      public class DateField extends AbstractDateField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(5)
      public class TimeField extends AbstractTimeField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(6)
      public class DateTimeField extends AbstractDateField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredHasTime() {
          return true;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(7)
      public class IntegerField extends AbstractIntegerField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(8)
      public class PlainIntegerField extends AbstractIntegerField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredGroupingUsed() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(9)
      public class LongField extends AbstractLongField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(10)
      public class PlainLongField extends AbstractLongField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredGroupingUsed() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(13)
      public class DoubleField extends AbstractDoubleField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(14)
      public class PlainDoubleField extends AbstractDoubleField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredGroupingUsed() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(15)
      public class PercentField extends AbstractDoubleField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredPercent() {
          return true;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(16)
      public class StringField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }
      }

      @Order(17)
      public class SmartField extends AbstractSmartField<Long> {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected void execChangedValue() {
          setSelectedValueInternal(getValue());
          setSelectedDisplayValueInternal(getDisplayText());
        }

        @Override
        public void execPrepareLookup(LookupCall call) throws ProcessingException {
          IComposerAttribute att = getAttributeField().getCheckedKey();
          if (att != null) {
            att.prepareLookup(call);
          }
        }
      }

      @Order(18)
      public class DummyField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Value");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
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
