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
package org.eclipse.scout.rt.client.ui.form.useradmin;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm.MainBox.GroupBox.NewPasswordField;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm.MainBox.GroupBox.OldPasswordField;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm.MainBox.GroupBox.RepeatPasswordField;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm.MainBox.OkButton;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.pwd.IPasswordManagementService;

public class DefaultPasswordForm extends AbstractForm {
  private String m_userId;

  public DefaultPasswordForm() {
    super();
  }

  public String getUserId() {
    return m_userId;
  }

  public void setUserId(String userId) {
    m_userId = userId;
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("ChangePassword");
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public OldPasswordField getOldPasswordField() {
    return getFieldByClass(OldPasswordField.class);
  }

  public NewPasswordField getNewPasswordField() {
    return getFieldByClass(NewPasswordField.class);
  }

  public RepeatPasswordField getRepeatPasswordField() {
    return getFieldByClass(RepeatPasswordField.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public void startChange() {
    startInternal(new ChangeHandler());
  }

  public void startReset() {
    startInternal(new ResetHandler());
  }

  @Order(20)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      public class OldPasswordField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("OldPassword");
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected boolean getConfiguredInputMasked() {
          return true;
        }
      }

      @Order(20)
      public class NewPasswordField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("NewPassword");
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected boolean getConfiguredInputMasked() {
          return true;
        }
      }

      @Order(30)
      public class RepeatPasswordField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("RepeatPassword");
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected boolean getConfiguredInputMasked() {
          return true;
        }
      }
    }

    @Order(40)
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    public class CancelButton extends AbstractCancelButton {
    }

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }
  }

  public class ResetHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      getOldPasswordField().setMandatory(false);
      getOldPasswordField().setVisible(false);
    }

    @Override
    protected void execStore() {
      if (getNewPasswordField().getValue() != null && !getNewPasswordField().getValue().equals(getRepeatPasswordField().getValue())) {
        throw new VetoException(ScoutTexts.get("PasswordsDoNotMatch"));
      }
      IPasswordManagementService svc = BEANS.get(IPasswordManagementService.class);
      svc.resetPassword(getUserId(), getNewPasswordField().getValue());
    }
  }

  public class ChangeHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
    }

    @Override
    protected void execStore() {
      if (getNewPasswordField().getValue() != null && !getNewPasswordField().getValue().equals(getRepeatPasswordField().getValue())) {
        throw new VetoException(ScoutTexts.get("PasswordsDoNotMatch"));
      }
      IPasswordManagementService svc = BEANS.get(IPasswordManagementService.class);
      svc.changePassword(getUserId(), getOldPasswordField().getValue(), getNewPasswordField().getValue());
    }
  }
}
