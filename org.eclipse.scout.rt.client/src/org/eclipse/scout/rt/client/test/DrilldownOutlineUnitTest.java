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
package org.eclipse.scout.rt.client.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.services.common.test.AbstractClientTest;
import org.eclipse.scout.rt.client.services.common.test.ClientTestUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.IBigIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.IDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class DrilldownOutlineUnitTest extends AbstractClientTest {

  private final Set<CompositeObject> visitedPages = new HashSet<CompositeObject>();
  private IPageTest[] m_pageTester;

  protected void clearVisitCache() {
    visitedPages.clear();
  }

  @Override
  public void setUp() {
    m_pageTester = SERVICES.getServices(IPageTest.class);
  }

  @Override
  public void run() throws Exception {
    clearVisitCache();
    for (IOutline outline : ClientTestUtility.getDesktop().getAvailableOutlines()) {
      if (outline.isVisible() && outline.isEnabled()) {
        ClientTestUtility.getDesktop().setOutline(outline);
        testPage(outline.getRootPage());
      }
    }
  }

  @Override
  public void tearDown() throws Throwable {
    clearVisitCache();
  }

  protected void testPage(IPage page) throws ProcessingException {
    if (page == null) {
      return;
    }
    if (page != page.getTree().getRootNode()) {
      Class<? extends IPage> pageClass = page.getClass();
      Class<? extends IPage> parentClass = null;
      if (page.getParentPage() != null) {
        parentClass = page.getParentPage().getClass();
      }
      CompositeObject key = new CompositeObject(parentClass, pageClass);
      if (visitedPages.contains(key)) {
        return;
      }
      else {
        visitedPages.add(key);
      }
    }
    setSubTitle(page.getCell().getText() + " [" + page.getClass().getSimpleName() + "]");
    try {
      if (page.isVisible() && page.isEnabled()) {
        page.getOutline().selectNode(page);

        // is there a specific test for this page?
        for (IPageTest tester : m_pageTester) {
          if (tester.canHandle(page)) {
            page = tester.testPage(page); // continue testing with the result
            break;
          }
        }

        if (page != null && page.isVisible() && page.isEnabled()) {
          if (page instanceof IPageWithTable<?>) {
            testPageWithTable((IPageWithTable<?>) page);
            addOkStatus(null);
          }
          else if (page instanceof IPageWithNodes) {
            testPageWithNodes((IPageWithNodes) page);
            addOkStatus(null);
          }
          else {
            addWarningStatus("unsupported page type: " + page.getClass().getCanonicalName());
          }
        }
      }
    }
    catch (Throwable t) {
      addErrorStatus(t.getMessage(), t);
    }
  }

  protected void testPageWithTable(IPageWithTable<?> page) throws ProcessingException {
    ISearchForm searchForm = page.getSearchFormInternal();
    P_LimitingResultSetSearchFormListener searchFormListener = null;
    if (searchForm != null) {
      searchFormListener = new P_LimitingResultSetSearchFormListener(searchForm);
      searchForm.addFormListener(searchFormListener);
      testSearchForm(page, searchForm);
      if (page.isSearchRequired()) {
        searchForm.doReset();
        fillMandatoryFields(searchForm);
        searchForm.doSaveWithoutMarkerChange();
      }
      searchForm.removeFormListener(searchFormListener);
    }
    if (page.getChildNodeCount() > 0) {
      IPage child = page.getChildPage(0);
      testPage(child);
    }
  }

  protected void testPageWithNodes(IPageWithNodes page) throws ProcessingException {
    for (IPage childPage : page.getChildPages()) {
      testPage(childPage);
    }
  }

  protected void testSearchForm(IPageWithTable<?> page, final ISearchForm searchForm) throws ProcessingException {
    if (searchForm == null) {
      return;
    }
    for (IFormField field : searchForm.getAllFields()) {
      if (field instanceof IValueField) {
        setSubTitle(StringUtility.join(" > ", StringUtility.nvl(searchForm.getTitle(), searchForm.getClass().getSimpleName()), field.getLabel()) + " [" + field.getClass().getSimpleName() + "]");
        try {
          searchForm.doReset();
          try {
            fillMandatoryFields(searchForm);
            if (fillValueField((IValueField) field)) {
              searchForm.doSaveWithoutMarkerChange();
              addOkStatus(null);
            }
          }
          catch (VetoException e) {
            addWarningStatus("Veto", null);
          }
        }
        catch (Throwable t) {
          addErrorStatus(t.getMessage(), t);
        }
      }
      else if (field instanceof IComposerField) {
        testComposerField(searchForm, (IComposerField) field);
      }
    }
  }

  protected void testComposerField(ISearchForm searchForm, IComposerField field) {
  }

  protected void fillMandatoryFields(ISearchForm searchForm) throws ProcessingException {
    if (searchForm == null) {
      return;
    }
    for (IFormField field : searchForm.getAllFields()) {
      if (field instanceof IValueField && ((IValueField) field).isMandatory()) {
        fillValueField((IValueField) field);
      }
    }
  }

  protected boolean fillValueField(IValueField<?> field) throws ProcessingException {
    boolean successful = false;
    if (field.isEnabled() && field.isVisible()) {
      if (field instanceof IBooleanField) {
        successful = fillBooleanField((IBooleanField) field);
      }
      else if (field instanceof IDateField) {
        successful = fillDateField((IDateField) field);
      }
      else if (field instanceof IBigDecimalField) {
        successful = fillBigDecimalField((IBigDecimalField) field);
      }
      else if (field instanceof IDoubleField) {
        successful = fillDoubleField((IDoubleField) field);
      }
      else if (field instanceof IListBox) {
        successful = fillListBox((IListBox) field);
      }
      else if (field instanceof IBigIntegerField) {
        successful = fillBigIntegerField((IBigIntegerField) field);
      }
      else if (field instanceof IIntegerField) {
        successful = fillIntegerField((IIntegerField) field);
      }
      else if (field instanceof ILongField) {
        successful = fillLongField((ILongField) field);
      }
      else if (field instanceof IRadioButtonGroup) {
        successful = fillRadioButtonGroup((IRadioButtonGroup) field);
      }
      else if (field instanceof ISmartField) {
        successful = fillSmartField((ISmartField) field);
      }
      else if (field instanceof IStringField) {
        successful = fillStringField((IStringField) field);
      }
      else if (field instanceof ITreeBox) {
        successful = fillTreeBox((ITreeBox<?>) field);
      }
      else {
        return false;
      }
    }
    return successful && field.isContentValid();
  }

  protected boolean fillBooleanField(IBooleanField field) {
    field.setValue(true);
    return field.isContentValid();
  }

  protected boolean fillDateField(IDateField field) {
    if (field.isHasTime()) {
      field.setValue(new Date());
    }
    else {
      field.setValue(DateUtility.truncDate(new Date()));
    }
    return field.isContentValid();
  }

  protected boolean fillBigDecimalField(IBigDecimalField field) {
    BigDecimal value = field.getMinValue();
    if (value == null) {
      value = field.getMaxValue();
    }
    if (value == null) {
      value = BigDecimal.ONE;
    }
    field.setValue(value);
    return field.isContentValid();
  }

  protected boolean fillDoubleField(IDoubleField field) {
    Double value = field.getMinValue();
    if (value == null) {
      value = field.getMaxValue();
    }
    if (value == null) {
      value = Double.valueOf(1.2345d);
    }
    if (value.doubleValue() == value.longValue()) {
      // test with fractional value
      value = Double.valueOf(value.doubleValue() + 0.2345d);
    }
    field.setValue(value);
    return true;
  }

  @SuppressWarnings("unchecked")
  protected boolean fillListBox(IListBox field) {
    ITable table = field.getTable();
    if (table.getRowCount() > 0) {
      for (ITableRow row : table.getRows()) {
        if (row.isEnabled()) {
          Object[] keyValues = row.getKeyValues();
          if (keyValues.length > 0) {
            field.checkKey(keyValues[0]);
            return true;
          }
        }
      }
    }
    return false;
  }

  protected boolean fillBigIntegerField(IBigIntegerField field) {
    BigInteger value = field.getMinValue();
    if (value == null) {
      value = field.getMaxValue();
    }
    if (value == null) {
      value = BigInteger.ONE;
    }
    field.setValue(value);
    return true;
  }

  protected boolean fillIntegerField(IIntegerField field) {
    Integer value = field.getMinValue();
    if (value == null) {
      value = field.getMaxValue();
    }
    if (value == null) {
      value = Integer.valueOf(42);
    }
    field.setValue(value);
    return true;
  }

  protected boolean fillLongField(ILongField field) {
    Long value = field.getMinValue();
    if (value == null) {
      value = field.getMaxValue();
    }
    if (value == null) {
      value = Long.valueOf(192L);
    }
    field.setValue(value);
    return true;
  }

  protected boolean fillRadioButtonGroup(IRadioButtonGroup field) throws ProcessingException {
    IButton[] buttons = field.getButtons();
    if (buttons != null) {
      for (IButton button : buttons) {
        if (button.isVisible() && button.isEnabled()) {
          button.doClick();
          return true;
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  protected boolean fillSmartField(ISmartField field) throws ProcessingException {
    LookupRow[] lookupRows = field.callBrowseLookup(null, 10);
    if (lookupRows != null && lookupRows.length > 0) {
      field.setValue(lookupRows[0].getKey());
      return true;
    }
    return false;
  }

  protected boolean fillStringField(IStringField field) {
    field.setValue("abcd1234");
    return field.isContentValid();
  }

  @SuppressWarnings("unchecked")
  protected boolean fillTreeBox(ITreeBox field) throws ProcessingException {
    ITreeNode node = getFirstSelectableNode(field.getTree().getRootNode());
    if (node != null) {
      field.setValue(node.getPrimaryKey());
      return true;
    }
    return false;
  }

  protected ITreeNode getFirstSelectableNode(ITreeNode... nodes) {
    if (nodes != null) {
      for (ITreeNode node : nodes) {
        if (node != null) {
          if (node.isEnabled() && node.isVisible() && node.getParentNode() != null) {
            return node;
          }
          else {
            ITreeNode selecteableNode = getFirstSelectableNode(node.getChildNodes());
            if (selecteableNode != null) {
              return selecteableNode;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  protected String getConfiguredTitle() {
    return "outline drill-down";
  }

  private static class P_LimitingResultSetSearchFormListener implements FormListener {
    final ISearchForm searchForm;

    public P_LimitingResultSetSearchFormListener(ISearchForm searchForm) {
      this.searchForm = searchForm;
    }

    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      if (e.getType() == FormEvent.TYPE_STORE_AFTER) {
        // TODO fill out searchForm
      }
    }
  }
}
