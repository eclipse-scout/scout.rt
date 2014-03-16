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
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.UnsavedFormChangesForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.desktop.UnsavedFormChangesForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.desktop.UnsavedFormChangesForm.MainBox.OpenFormsField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

@ClassId("b5783a42-9fd8-4043-afc1-6e744dad9c8f")
public class UnsavedFormChangesForm extends AbstractForm {
  private final List<IForm> m_forms;

  public UnsavedFormChangesForm(List<IForm> forms) throws ProcessingException {
    m_forms = forms;
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("UnsavedChangesTitle");
  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public OpenFormsField getOpenFormsField() {
    return getFieldByClass(OpenFormsField.class);
  }

  @Order(10.0)
  @ClassId("7c89cc91-2c09-472b-af3b-ee93b50caaad")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(10.0)
    public class LabelField extends AbstractLabelField {

      @Override
      protected void execInitField() throws ProcessingException {
        setValue(TEXTS.get("SaveChangesOfSelectedItems"));
      }

      @Override
      protected int getConfiguredGridH() {
        return 1;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

    }

    @Order(20.0)
    @ClassId("84f2a9cf-bce5-4379-aede-11d07b21d3fb")
    public class OpenFormsField extends AbstractListBox<IForm> {

      @Override
      protected void execInitField() throws ProcessingException {
        checkAllActiveKeys();
      }

      @Override
      protected Class<? extends LookupCall> getConfiguredLookupCall() {
        return UnsavedFormsLookupCall.class;
      }

      @Override
      protected int getConfiguredGridH() {
        return 5;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

    }

    @Order(20.0)
    @ClassId("caca3d68-b8cc-4cb0-a35c-5b8ccbcc3745")
    public class OkButton extends AbstractOkButton {
    }

    @Order(30.0)
    @ClassId("50c8526a-333f-4878-9876-b48f2b583d88")
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {

    @Override
    protected void execPostLoad() throws ProcessingException {
      touch();
    }

    @Override
    protected void execStore() throws ProcessingException {
      IForm[] forms = getOpenFormsField().getValue();
      if (forms != null) {
        for (IForm f : forms) {
          f.doOk();
        }
      }
    }

  }

  @ClassId("70052229-e6e5-43f3-bac5-cabe6e4525d3")
  public static class UnsavedFormsLookupCall extends LocalLookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
      List<LookupRow> formRows = new ArrayList<LookupRow>();
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null) {
        for (IForm f : desktop.getUnsavedForms()) {
          String text = StringUtility.nvl(f.getTitle(), f.getClass().getName());
          formRows.add(new LookupRow(f, text));
        }
      }
      return formRows;
    }

  }

}
