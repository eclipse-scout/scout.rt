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
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * <h3>DefaultWizardContainerForm</h3> A container form containing a wizard form area (current step) a status area. see
 * {@link AbstractWizard#execCreateContainerForm()}
 *
 * @since 24.11.2009
 */
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
    public class WizardProgressField extends AbstractWizardProgressField {
    }

    @Order(20)
    public class ContentBox extends AbstractGroupBox {

      @Override
      protected boolean getConfiguredBorderVisible() {
        return false;
      }

      @Order(10)
      public class WrappedWizardForm extends AbstractWrappedFormField<IForm> {

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }
      }
    }

    @Order(30)
    public class WizardPreviousStepButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardBackButton");
      }

      @Override
      protected void execClickAction() {
        getWizard().doPreviousStep();
      }
    }

    @Order(40)
    public class WizardNextStepButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardNextButton");
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
    public class WizardFinishButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardFinishButton");
      }

      @Override
      protected void execClickAction() {
        getWizard().doFinish();
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ENTER;
      }
    }

    @Order(60)
    public class WizardCancelButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardCancelButton");
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
    public class WizardSuspendButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardSuspendButton");
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
    public class WizardResetButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ResetButton");
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
    }
  }
}
