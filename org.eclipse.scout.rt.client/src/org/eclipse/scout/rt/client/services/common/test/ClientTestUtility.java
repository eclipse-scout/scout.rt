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
package org.eclipse.scout.rt.client.services.common.test;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.BlockingCondition;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractSearchForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.osgi.framework.Bundle;

public final class ClientTestUtility {

  private ClientTestUtility() {
  }

  public static void sleep(final int seconds) {
    if (seconds <= 0) {
      return;
    }
    final BlockingCondition bc = new BlockingCondition(true);
    new ClientSyncJob("sleep", ClientSyncJob.getCurrentSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        bc.release();
      }
    }.schedule(1000L * seconds);
    try {
      bc.waitFor();
    }
    catch (InterruptedException e) {
      // nop
    }
  }

  public static String getNowAsString() {
    return "" + new Date();
  }

  public static Date getNowAsDate() {
    return new Date();
  }

  @SuppressWarnings("unchecked")
  public static void searchEntityInOutline(AbstractPageWithTable<?> p, String formDataFieldId, long value) throws ProcessingException {
    AbstractSearchForm searchForm = (AbstractSearchForm) p.getSearchFormInternal();
    searchForm.rebuildSearchFilter();
    SearchFilter f = searchForm.getSearchFilter();
    f.clear();
    ((AbstractValueFieldData) f.getFormData().getFieldById(formDataFieldId)).setValue(value);
    f.setCompleted(true);
    p.getSearchFormInternal().doReset();
    p.reloadPage();
  }

  @SuppressWarnings("unchecked")
  public static void searchEntityInOutline(AbstractPageWithTable<?> p, String formDataFieldId, String value) throws ProcessingException {
    AbstractSearchForm searchForm = (AbstractSearchForm) p.getSearchFormInternal();
    searchForm.rebuildSearchFilter();
    SearchFilter f = searchForm.getSearchFilter();
    f.clear();
    ((AbstractValueFieldData) f.getFormData().getFieldById(formDataFieldId)).setValue(value);
    f.setCompleted(true);
    p.getSearchFormInternal().doReset();
    p.reloadPage();
  }

  public static IClientSession getClientSession() {
    return ClientSyncJob.getCurrentSession();
  }

  public static Bundle getClientBundle() {
    return ClientSyncJob.getCurrentSession().getBundle();
  }

  public static IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  public static String getFormsPackage() {
    return getClientBundle().getSymbolicName() + ".ui.forms";
  }

  /**
   * @rn aho, 09.03.2009:
   * @return the root page of the desired outline.
   */
  public static IPage gotoOutline(Class<? extends AbstractOutline> outlineClass) {
    IPage rootPage = null;
    IOutline[] availableOutlines = getDesktop().getAvailableOutlines();
    for (IOutline outline : availableOutlines) {

      if (outline.getClass().isAssignableFrom(outlineClass)) {
        getDesktop().setOutline(outline);
        System.out.println("Selected Outline: " + outline.getTitle());
        outline.releaseUnusedPages();
        rootPage = outline.getRootPage();
      }

    }
    return rootPage;
  }

  /**
   * @rn aho, 09.03.2009:
   * @return return the first child page of the given class, iff exists.
   */
  @SuppressWarnings("unchecked")
  public static <T extends IPage> T gotoChildPage(IPage parentPage, Class<T> childPageClass) {
    T childPage = null;
    IPage[] childPages = parentPage.getChildPages();
    for (IPage page : childPages) {
      if (childPageClass.isAssignableFrom(page.getClass())) {
        IOutline outline = getDesktop().getOutline();
        outline.selectNode(page);
        System.out.println("Selected Page: " + page);
        outline.releaseUnusedPages();
        childPage = (T) page;
      }

    }
    return childPage;
  }

  /**
   * @rn aho, 12.03.2009:
   * @return return the first child page of the given class, iff exists.
   */
  public static <T extends IPage> T gotoChildPage(Class<T> childPageClass) {
    IPage selectedNode = getDesktop().getOutline().getActivePage();
    return gotoChildPage(selectedNode, childPageClass);
  }

  @SuppressWarnings("unchecked")
  public static void fillForm(AbstractForm form, long testNr) throws ProcessingException {
    for (IFormField formField : form.getAllFields()) {
      if (formField.isEnabled() && formField.isVisible()) {
        if (formField instanceof AbstractValueField) {
          if (((AbstractValueField<?>) formField).getValue() == null) {
            // String Field
            if (formField instanceof AbstractStringField) {
              ((AbstractStringField) formField).setValue("test " + testNr);
              // Smart Field - set to 1st value
            }
            else if (formField instanceof AbstractSmartField) {
              LookupCall lookupCall = null;
              // SQL Lookup
              if (((AbstractSmartField<?>) formField).getLookupCall() != null) {
                lookupCall = ((AbstractSmartField<?>) formField).getLookupCall();
                ((AbstractSmartField<?>) formField).prepareKeyLookup(lookupCall, null);
                ((AbstractSmartField<?>) formField).prepareTextLookup(lookupCall, "abc");
                ((AbstractSmartField<?>) formField).prepareBrowseLookup(lookupCall, "abc", TriState.TRUE);
              }
              // CodeType Lookup
              else if (((AbstractSmartField<?>) formField).getCodeTypeClass() != null) {
                lookupCall = new CodeLookupCall(((AbstractSmartField<?>) formField).getCodeTypeClass());
              }
              if (lookupCall == null) {
                System.out.println("WARNING: Lookup Call for Field " + formField.getFieldId() + "is null!");
              }
              else {
                lookupCall.setActive(TriState.TRUE);
                lookupCall.setMaxRowCount(100);
                /* Test smartfield Key and Text */
                lookupCall.getDataByKey();
                lookupCall.getDataByText();
                /* Load data by all */
                final LookupRow[] rs = lookupCall.getDataByAll();
                if (rs.length > 0) {
                  ((AbstractSmartField) formField).setValue(rs[0].getKey());
                }
              }
              // Number Field
            }
            else if (formField instanceof AbstractNumberField) {
              ((AbstractNumberField) formField).setValue(42L);
              // Big Decimal Field
            }
            else if (formField instanceof AbstractBigDecimalField) {
              ((AbstractBigDecimalField) formField).setValue(BigDecimal.valueOf(42.42));
              // Date Field
            }
            else if (formField instanceof AbstractDateField) {
              ((AbstractDateField) formField).setValue(new Date());
            }
            else if (formField instanceof AbstractListBox) {
              LookupCall lookupCall = null;
              // SQL Lookup
              if (((AbstractListBox<?>) formField).getLookupCall() != null) {
                lookupCall = ((AbstractListBox<?>) formField).getLookupCall();
                ((AbstractListBox<?>) formField).prepareLookupCall(lookupCall);
              }
              // CodeType Lookup
              else if (((AbstractListBox<?>) formField).getCodeTypeClass() != null) {
                lookupCall = new CodeLookupCall(((AbstractListBox<?>) formField).getCodeTypeClass());
              }
              if (lookupCall == null) {
                System.out.println("WARNING: Lookup Call for Field " + formField.getFieldId() + "is null!");
              }
              else {
                lookupCall.setActive(TriState.TRUE);
                lookupCall.setMaxRowCount(100);
                /* Test smartfield Key and Text */
                lookupCall.getDataByKey();
                lookupCall.getDataByText();
                /* Load data by all */
                final LookupRow[] rs = lookupCall.getDataByAll();
                if (rs.length > 0) {
                  if ((((AbstractListBox) formField)).getCheckedKeyCount() == 0) {
                    ((AbstractListBox) formField).checkKey(rs[0].getKey());
                  }
                }
              }
            }
          }
        }
      }
    }
  }

}
