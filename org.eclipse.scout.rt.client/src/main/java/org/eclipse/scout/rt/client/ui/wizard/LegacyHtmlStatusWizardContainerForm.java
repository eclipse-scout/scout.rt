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
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.SplitBox;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.SplitBox.ContentBox;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.SplitBox.ContentBox.WrappedWizardForm;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.SplitBox.StatusBox;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.SplitBox.StatusBox.StatusField;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.WizardCancelButton;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.WizardFinishButton;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.WizardNextStepButton;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.WizardPreviousStepButton;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.WizardResetButton;
import org.eclipse.scout.rt.client.ui.wizard.LegacyHtmlStatusWizardContainerForm.MainBox.WizardSuspendButton;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * <h3>LegacyHtmlStatusWizardContainerForm</h3> A container form containing a wizard form area (current step) a status
 * area. see {@link AbstractWizard#execCreateContainerForm()}
 *
 * @since 24.11.2009
 * @deprecated This form uses a HTML provider to indicate the wizard progress. It should be replaced by
 *             {@link DefaultWizardContainerForm} which uses a {@link AbstractWizardProgressField} instead. This class
 *             will be removed in Scout 6.1.
 */
@Deprecated
@ClassId("422da96c-0da2-4020-a905-7bb594135a07")
public class LegacyHtmlStatusWizardContainerForm extends AbstractWizardContainerForm {

  public LegacyHtmlStatusWizardContainerForm(IWizard wizard) {
    this(wizard, true);
  }

  public LegacyHtmlStatusWizardContainerForm(IWizard wizard, boolean callInitializer) {
    super(wizard, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public SplitBox getSplitBox() {
    return getFieldByClass(SplitBox.class);
  }

  public ContentBox getContentBox() {
    return getFieldByClass(ContentBox.class);
  }

  public StatusBox getStatusBox() {
    return getFieldByClass(StatusBox.class);
  }

  public WrappedWizardForm getWrappedWizardForm() {
    return getFieldByClass(WrappedWizardForm.class);
  }

  public StatusField getStatusField() {
    return getFieldByClass(StatusField.class);
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
  @ClassId("c303615d-ad82-4ef4-9b04-dfd6e09c85be")
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

    @Order(20)
    @ClassId("fef9cb06-b4b8-4ffd-88d4-1818a0cbd1a7")
    public class SplitBox extends AbstractSplitBox {

      @Override
      protected double getConfiguredSplitterPosition() {
        return 0.75;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Order(10)
      @ClassId("f89b6186-3c82-4bdb-abb0-98157cf08f61")
      public class ContentBox extends AbstractGroupBox {

        @Override
        protected boolean getConfiguredBorderVisible() {
          return false;
        }

        @Order(10)
        @ClassId("2d686e4f-ee2c-450c-93cd-0a81f719dd01")
        public class WrappedWizardForm extends AbstractWrappedFormField<IForm> {

          @Override
          protected int getConfiguredGridW() {
            return 2;
          }
        }
      }

      @Order(20)
      @ClassId("fb682cc7-1d9c-43eb-a2fa-239866dca1ff")
      public class StatusBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected boolean getConfiguredBorderVisible() {
          return false;
        }

        @Override
        protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
          super.injectFieldsInternal(fields);
          // TODO [5.2] bsh: Inject info boxes and groups here
        }

        @Order(20)
        @ClassId("9b04f649-72db-4584-95b3-ad4df4903454")
        public class StatusField extends AbstractWizardStatusField {

          @Override
          protected int getConfiguredGridW() {
            return 2;
          }

          @Override
          protected int getConfiguredGridH() {
            return 2;
          }

          @Override
          protected void execAppLinkAction(String ref) {
            if (LegacyHtmlStatusWizardContainerForm.this.getWizard() != null) {
              LegacyHtmlStatusWizardContainerForm.this.getWizard().doAppLinkAction(ref);
            }
            else {
              super.execAppLinkAction(ref);
            }
          }
        }
      }
    }

    @Order(30)
    @ClassId("920f4327-88f6-4ecf-a435-7a46e90bee1d")
    public class WizardPreviousStepButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardBackButton");
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 1;
      }

      @Override
      protected void execClickAction() {
        getWizard().doPreviousStep();
      }
    }

    @Order(40)
    @ClassId("32568b24-346c-45c7-b86f-20db0a8d652c")
    public class WizardNextStepButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardNextButton");
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 1;
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ENTER;
      }

      @Override
      protected void execClickAction() {
        getWizard().doNextStep();
      }
    }

    @Order(50)
    @ClassId("b22178eb-6727-4f05-b5ec-930475f4f3b4")
    public class WizardFinishButton extends AbstractButton implements IWizardAction {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardFinishButton");
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 1;
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ENTER;
      }

      @Override
      protected void execClickAction() {
        getWizard().doFinish();
      }
    }

    @Order(60)
    @ClassId("1fa42cc2-9006-4c47-b420-f6c710ac176b")
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
      protected int getConfiguredHorizontalAlignment() {
        return -1;
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
    @ClassId("93ddf703-c181-4c4c-acc3-420ea858fffb")
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
      protected int getConfiguredHorizontalAlignment() {
        return -1;
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
      protected int getConfiguredHorizontalAlignment() {
        return -1;
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
