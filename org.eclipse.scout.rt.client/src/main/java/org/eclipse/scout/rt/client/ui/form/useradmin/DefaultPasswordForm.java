/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.useradmin;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
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
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.services.common.pwd.IPasswordManagementService;

@ClassId("5bcb48f0-9b72-4f28-9c08-038cd5d9a1c4")
public class DefaultPasswordForm extends AbstractForm {
  private String m_userId;

  public String getUserId() {
    return m_userId;
  }

  public void setUserId(String userId) {
    m_userId = userId;
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("ChangePassword");
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
  @ClassId("02df65d9-252a-4c24-a232-27bb0924a1b1")
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    @ClassId("b44b8225-ce40-448c-a908-4ef8dc4df876")
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      @ClassId("b27cada2-acb5-4702-a2c5-87e529fb26bc")
      public class OldPasswordField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("OldPassword");
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
      @ClassId("9fb80aa1-3c3c-4881-8395-7155d69d4425")
      public class NewPasswordField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("NewPassword");
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
      @ClassId("14d4143c-ceb3-4a3a-8c83-cfeb09b0727f")
      public class RepeatPasswordField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("RepeatPassword");
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
    @ClassId("d228bd1e-a052-4036-8ca4-7b0ec4ac42ae")
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    @ClassId("0a491aaf-cf8d-49ce-91d9-f9e2a5ee86f9")
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
        throw new VetoException(TEXTS.get("PasswordsDoNotMatch"));
      }
      IPasswordManagementService svc = BEANS.get(IPasswordManagementService.class);
      svc.resetPassword(getUserId(), getNewPasswordField().getValue().toCharArray());
      resetSessionIfCurrentUser(svc);
    }
  }

  public class ChangeHandler extends AbstractFormHandler {
    @Override
    protected void execStore() {
      if (getNewPasswordField().getValue() != null && !getNewPasswordField().getValue().equals(getRepeatPasswordField().getValue())) {
        throw new VetoException(TEXTS.get("PasswordsDoNotMatch"));
      }
      IPasswordManagementService svc = BEANS.get(IPasswordManagementService.class);
      svc.changePassword(getUserId(), getOldPasswordField().getValue().toCharArray(), getNewPasswordField().getValue().toCharArray());
      resetSessionIfCurrentUser(svc);
    }
  }

  protected void resetSessionIfCurrentUser(IPasswordManagementService svc) {
    //owasp: reset session
    IClientSession session = (IClientSession) ISession.CURRENT.get();
    String userName = svc.getUsernameFor(getUserId());
    String myNameIs = BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
    if (myNameIs.equals(userName)) {
      ModelJobs.schedule((IRunnable) session::stop, ModelJobs.newInput(ClientRunContexts.empty().withSession(session, false)));
    }
  }
}
