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
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.ContentBox;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.ContentBox.WrappedWizardForm;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardCancelButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardFinishButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardNextStepButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardPreviousStepButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardProgressField;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardResetButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardSuspendButton;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * <h3>DefaultWizardContainerForm</h3> A container form containing a wizard form area (current step) a status area. see
 * {@link AbstractWizard#execCreateContainerForm()}
 *
 * @since 24.11.2009
 */
@ClassId("dc649273-10aa-4677-8b8d-11ed0965d644")
public class DefaultWizardContainerForm extends AbstractWizardContainerForm {

  public DefaultWizardContainerForm(IWizard wizard) {
    this(wizard, true);
  }

  public DefaultWizardContainerForm(IWizard wizard, boolean callInitializer) {
    super(wizard, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public WizardProgressField getWizardProgressField() {
    return getFieldByClass(WizardProgressField.class);
  }

  public ContentBox getContentBox() {
    return getFieldByClass(ContentBox.class);
  }

  public WrappedWizardForm getWrappedWizardForm() {
    return getFieldByClass(WrappedWizardForm.class);
  }

  @Override
  public IWizardAction getWizardCancelButton() {
    return getFieldByClass(WizardCancelButton.class);
  }

  @Override
  public IWizardAction getWizardSuspendButton() {
    return getFieldByClass(WizardSuspendButton.class);
  }

  @Override
  public IWizardAction getWizardResetButton() {
    return getFieldByClass(WizardResetButton.class);
  }

  @Override
  public IWizardAction getWizardPreviousStepButton() {
    return getFieldByClass(WizardPreviousStepButton.class);
  }

  @Override
  public IWizardAction getWizardNextStepButton() {
    return getFieldByClass(WizardNextStepButton.class);
  }

  @Override
  public IWizardAction getWizardFinishButton() {
    return getFieldByClass(WizardFinishButton.class);
  }

  @Override
  protected IForm getInnerWizardForm() {
    return getWrappedWizardForm().getInnerForm();
  }

  @Override
  protected void setInnerWizardForm(IForm form) {
    getWrappedWizardForm().setInnerForm(form, false);
  }

  @Override
  public void startWizard() {
    startInternal(new WizardHandler());
  }

  @Order(10)
  @ClassId("051f6ae5-8a54-4f6c-8213-9b1e8f280a2a")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridW() {
      return 3;
    }

    @Override
    protected int getConfiguredGridH() {
      return 32;
    }

    @Override
    protected boolean getConfiguredGridUseUiHeight() {
      return false;
    }

    @Override
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(10)
    @ClassId("41e660ac-c25d-47a0-80f8-ebcaa0493e87")
    public class WizardProgressField extends AbstractWizardProgressField {
    }

    @Order(20)
    @ClassId("bcae045d-410d-4ffd-bae7-d39d55de9b17")
    public class ContentBox extends AbstractGroupBox {

      @Override
      protected boolean getConfiguredBorderVisible() {
        return false;
      }

      @Order(10)
      @ClassId("954d9fb4-8d8f-4590-a4f1-9b4f3f15f2d1")
      public class WrappedWizardForm extends AbstractWrappedFormField<IForm> {

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }
      }
    }

    @Order(30)
    @ClassId("6da76125-d126-49eb-a6c5-081cb5635c4e")
    public class WizardPreviousStepButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("WizardBackButton");
      }

      @Override
      protected void execClickAction() {
        getWizard().doPreviousStep();
      }
    }

    @Order(40)
    @ClassId("08bc7570-d67f-4d3c-98c0-c0a3af9b43ce")
    public class WizardNextStepButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("WizardNextButton");
      }

      @Override
      protected void execClickAction() {
        getWizard().doNextStep();
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ENTER;
      }
    }

    @Order(50)
    @ClassId("e8191b4e-d1bf-464e-ac87-c634f246daf1")
    public class WizardFinishButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("WizardFinishButton");
      }

      @Override
      protected void execClickAction() {
        getWizard().doFinish();
      }

      /**
       * This is intentionally the same as in {@link WizardNextStepButton}: If both buttons are visible, the leftmost
       * (lowest {@link Order}) will capture this key stroke. If only one is visible, it alone will capture it.
       */
      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ENTER;
      }
    }

    @Order(60)
    @ClassId("2424b287-6397-4aaa-89d2-69ec4cb293bb")
    public class WizardCancelButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("WizardCancelButton");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ESCAPE;
      }

      @Override
      protected void execClickAction() {
        getWizard().doCancel();
      }
    }

    @Order(70)
    @ClassId("0602c31b-eedc-42f0-a739-a55f39e8d35f")
    public class WizardSuspendButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("WizardSuspendButton");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ESCAPE;
      }

      @Override
      protected void execClickAction() {
        getWizard().doSuspend();
      }
    }

    @Order(80)
    @ClassId("e6d08552-7384-45ea-ae91-db37bfa33f98")
    public class WizardResetButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("ResetButton");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected void execClickAction() {
        getWizard().doReset();
      }
    }
  }

  public class WizardHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      setInnerWizardForm(getWizard() == null ? null : getWizard().getWizardForm());
      // When displayed as dialog, the root group box border should be visible. Otherwise, the form title
      // and the wizard progress field would be too close.
      if (getDisplayHint() == DISPLAY_HINT_DIALOG) {
        getMainBox().setBorderDecoration(IGroupBox.BORDER_DECORATION_EMPTY);
      }
    }
  }
}
