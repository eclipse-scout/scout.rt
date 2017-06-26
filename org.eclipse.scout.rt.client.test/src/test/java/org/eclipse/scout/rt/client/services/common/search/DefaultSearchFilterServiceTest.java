/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.services.common.search.DefaultSearchFilterServiceTest.MyComposerField.CarEntity;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerDisplayTextBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.DataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DefaultSearchFilterService}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DefaultSearchFilterServiceTest {
  private DefaultSearchFilterService m_searchFilterService;
  private SearchFilter m_searchFilter;
  private static String LABEL = "Label";

  @Before
  public void setUp() {
    m_searchFilterService = new DefaultSearchFilterService();
    m_searchFilter = new SearchFilter();
  }

  @Test
  public void testListBox() {
    AbstractListBox<Long> listBox = new MyListBox();
    listBox.initField();
    runBasicAsserts(listBox);
    listBox.setValue(new HashSet<Long>(Arrays.asList(2L)));
    m_searchFilterService.applySearchDelegate(listBox, m_searchFilter, false);
    Assert.assertEquals(LABEL + " " + TEXTS.get("LogicIn") + " " + listBox.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testTreeBox() {
    AbstractTreeBox<Long> treeBox = new MyTreeBox();
    treeBox.initField();
    runBasicAsserts(treeBox);
    treeBox.setValue(new HashSet<Long>(Arrays.asList(2L)));
    m_searchFilterService.applySearchDelegate(treeBox, m_searchFilter, false);
    Assert.assertEquals(LABEL + " " + TEXTS.get("LogicIn") + " " + treeBox.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testLabelField() {
    AbstractLabelField labelField = new MyLabelField();
    labelField.initField();
    runBasicAsserts(labelField);
    labelField.setValue("value");
    m_searchFilterService.applySearchDelegate(labelField, m_searchFilter, false);
    Assert.assertEquals(LABEL + " " + TEXTS.get("LogicLike") + " " + labelField.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testStringField() {
    AbstractStringField stringField = new MyStringField();
    stringField.initField();
    runBasicAsserts(stringField);
    stringField.setValue("value");
    m_searchFilterService.applySearchDelegate(stringField, m_searchFilter, false);
    Assert.assertEquals(LABEL + " " + TEXTS.get("LogicLike") + " " + stringField.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testHtmlField() {
    AbstractHtmlField htmlField = new MyHTMLField();
    htmlField.initField();
    runBasicAsserts(htmlField);
    htmlField.setValue("value");
    m_searchFilterService.applySearchDelegate(htmlField, m_searchFilter, false);
    Assert.assertEquals(LABEL + " " + TEXTS.get("LogicLike") + " " + htmlField.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testBooleanField() {
    AbstractBooleanField booleanField = new MyBooleanField();
    booleanField.initField();
    runBasicAsserts(booleanField);
    booleanField.setValue(true);
    m_searchFilterService.applySearchDelegate(booleanField, m_searchFilter, false);
    Assert.assertEquals(LABEL, m_searchFilter.getDisplayTextsPlain());
    booleanField.setValue(false);
    m_searchFilterService.applySearchDelegate(booleanField, m_searchFilter, false);
    Assert.assertEquals(LABEL, m_searchFilter.getDisplayTextsPlain());

    m_searchFilter.clear();
    booleanField.setLabel(null);
    booleanField.setValue(true);
    m_searchFilterService.applySearchDelegate(booleanField, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
    booleanField.setValue(false);
    m_searchFilterService.applySearchDelegate(booleanField, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testRadioButtonGroup() {
    MyRadioButtonGroup radioButtonGroup = new MyRadioButtonGroup();
    radioButtonGroup.initField();
    runBasicAsserts(radioButtonGroup);
    radioButtonGroup.setValue(1L);
    m_searchFilterService.applySearchDelegate(radioButtonGroup, m_searchFilter, false);
    Assert.assertEquals(LABEL + "=" + radioButtonGroup.getRadioButton1().getLabel(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testValueField() {
    MyValueField field = new MyValueField();
    field.initField();
    runBasicAsserts(field);
    field.setValue(1L);
    m_searchFilterService.applySearchDelegate(field, m_searchFilter, false);
    Assert.assertEquals(LABEL + " " + TEXTS.get("LogicEQ") + " " + field.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testSequenceBox() {
    MySequenceBox seqBox = new MySequenceBox();
    seqBox.initField();

    MySequenceBox.MyStringField stringField = seqBox.getMyStringField();
    runBasicAsserts(stringField);

    m_searchFilter.clear();
    seqBox = new MySequenceBoxWithLabel();
    seqBox.initField();
    stringField = seqBox.getMyStringField();
    m_searchFilterService.applySearchDelegate(stringField, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
    stringField.setLabel(LABEL);
    m_searchFilterService.applySearchDelegate(stringField, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
    stringField.setValue("value");
    m_searchFilterService.applySearchDelegate(stringField, m_searchFilter, false);
    Assert.assertEquals(seqBox.getLabel() + " " + TEXTS.get("LogicLike") + " " + stringField.getDisplayText(), m_searchFilter.getDisplayTextsPlain());
  }

  @Test
  public void testComposerField() {
    MyComposerField composer = new MyComposerField();
    composer.initField();
    runBasicAsserts(composer);

    CarEntity carEntity = composer.new CarEntity();
    ITreeNode carNode = composer.addEntityNode(
        composer.getTree().getRootNode(),
        carEntity,
        true,
        Collections.emptyList(),
        new ArrayList<String>());

    composer.addAttributeNode(carNode,
        carEntity.new ColorAttribute(),
        DataModelConstants.AGGREGATION_NONE,
        DataModelAttributeOp.create(DataModelConstants.OPERATOR_EQ),
        CollectionUtility.arrayList("blue key"),
        CollectionUtility.arrayList("blue value"));
    m_searchFilterService.applySearchDelegate(composer, m_searchFilter, false);
    StringBuilder result = new StringBuilder();
    new ComposerDisplayTextBuilder().build(composer.getTree().getRootNode(), result, "");
    Assert.assertEquals(result.toString().trim(), m_searchFilter.getDisplayTextsPlain());
  }

  public void runBasicAsserts(IFormField f) {
    m_searchFilterService.applySearchDelegate(f, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
    f.setLabel(LABEL);
    m_searchFilterService.applySearchDelegate(f, m_searchFilter, false);
    Assert.assertEquals("", m_searchFilter.getDisplayTextsPlain());
  }

  public class MyListBox extends AbstractListBox<Long> {
    @SuppressWarnings("unchecked")
    @Override
    protected List<? extends ILookupRow<Long>> execLoadTableData() {
      List<ILookupRow<Long>> data = new LinkedList<ILookupRow<Long>>();
      data.add(new LookupRow(1L, "Element 1"));
      data.add(new LookupRow(2L, "Element 2"));
      data.add(new LookupRow(3L, "Element 3"));
      data.add(new LookupRow(4L, "Element 4"));
      data.add(new LookupRow(5L, "Element 5"));
      return data;
    }
  }

  public class MyTreeBox extends AbstractTreeBox<Long> {
    @SuppressWarnings("unchecked")
    @Override
    protected void execLoadChildNodes(ITreeNode parentNode) {
      List<ILookupRow<Long>> data = new LinkedList<ILookupRow<Long>>();
      data.add(new LookupRow(1L, "Element 1"));
      data.add(new LookupRow(2L, "Element 1a").withParentKey(1L));
      data.add(new LookupRow(3L, "Element 1b").withParentKey(1L));
      data.add(new LookupRow(4L, "Element 2"));
      data.add(new LookupRow(5L, "Element 2a").withParentKey(4L));

      List<ITreeNode> children = getTreeNodeBuilder().createTreeNodes(data, ITreeNode.STATUS_NON_CHANGED, false);
      getTree().removeAllChildNodes(parentNode);
      getTree().addChildNodes(parentNode, children);
      parentNode.setChildrenLoaded(true);
    }
  }

  public class MyLabelField extends AbstractLabelField {
  }

  public class MyHTMLField extends AbstractHtmlField {
  }

  public class MyStringField extends AbstractStringField {
  }

  public class MyBooleanField extends AbstractBooleanField {
  }

  public class MyRadioButtonGroup extends AbstractRadioButtonGroup<Long> {

    public RadioButton1Button getRadioButton1() {
      return getFieldByClass(RadioButton1Button.class);
    }

    @Order(10)
    public class RadioButton1Button extends AbstractRadioButton {

      @Override
      protected String getConfiguredLabel() {
        return "RadioButton1";
      }

      @Override
      protected Object getConfiguredRadioValue() {
        return 1L;
      }
    }
  }

  public class MyValueField extends AbstractValueField<Long> {
  }

  public class MySequenceBox extends AbstractSequenceBox {

    public MyStringField getMyStringField() {
      return getFieldByClass(MyStringField.class);
    }

    @Order(10)
    public class MyStringField extends AbstractStringField {
    }
  }

  public class MySequenceBoxWithLabel extends MySequenceBox {
    @Override
    protected String getConfiguredLabel() {
      return "SeqLabel";
    }
  }

  public class MyComposerField extends AbstractComposerField {
    @Order(80)
    public class CarEntity extends AbstractDataModelEntity {
      private static final long serialVersionUID = 1L;

      @Override
      public String getConfiguredText() {
        return "Auto";
      }

      @Order(10)
      public class ColorAttribute extends AbstractDataModelAttribute {
        private static final long serialVersionUID = 1L;

        @Override
        public String getConfiguredText() {
          return "Color";
        }

        @Override
        public int getConfiguredType() {
          return IDataModelAttribute.TYPE_STRING;
        }
      }
    }
  }
}
