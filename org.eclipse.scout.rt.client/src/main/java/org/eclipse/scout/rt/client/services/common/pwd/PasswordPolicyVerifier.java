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
package org.eclipse.scout.rt.client.services.common.pwd;

import java.util.Date;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.pwd.IPasswordManagementService;

/**
 * Runs the password verification process
 */
public class PasswordPolicyVerifier {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PasswordPolicyVerifier.class);

  /**
   * Calls {@link IPasswordManagementService#getPasswordExpirationDate(String)} to check whether the password has
   * expired. When desired, warns the user in advance about the expiration. If expired, calls the
   * {@link DefaultPasswordForm#startChange()} and - when closed - re-checks the expiry date. When still expired, exits
   * the application (scout session).
   *
   * @param warnInAdvanceDays
   *          number of days before the expiry when a warning shall occur, -1 to omit this feature
   * @return true if the password is not expired, false if - after all - the password has expired. Normally when
   *         returned false, the application quits.
   */
  public boolean verify(String userId, int warnInAdvanceDays) {
    IPasswordManagementService service = BEANS.get(IPasswordManagementService.class);
    if (service == null) {
      LOG.error("missing client service proxy for " + IPasswordManagementService.class.getName() + "; check plugin extensions");
      return false;
    }
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    if (desktop == null) {
      LOG.error("desktop is null");
      return false;
    }
    if (!desktop.isOpened()) {
      LOG.error("desktop is available, but there is not yet a GUI attached. Make sure to calll this verifier at earliest in the Desktop.execGuiAvailable callback");
      return false;
    }
    try {
      boolean changeNow = false;
      Date now = new Date();
      Date expiryDate = service.getPasswordExpirationDate(userId);
      if (expiryDate.after(now)) {
        // not expired
        long remainDays = (expiryDate.getTime() - now.getTime()) / 3600000L / 24L;
        if (remainDays < warnInAdvanceDays) {
          String header;
          if (remainDays == 0) {
            header = ScoutTexts.get("PasswordWillExpireHeaderX", ScoutTexts.get("Today"));
          }
          else if (remainDays == 1) {
            header = ScoutTexts.get("PasswordWillExpireHeaderX", ScoutTexts.get("Tomorrow"));
          }
          else {
            header = ScoutTexts.get("PasswordWillExpireHeaderX", ScoutTexts.get("InDaysX", "" + remainDays));
          }

          int answer = MessageBoxes.createYesNoCancel().withHeader(header).withBody(ScoutTexts.get("PasswordWillExpireInfo")).show();
          if (answer == MessageBox.YES_OPTION) {
            changeNow = true;
          }
        }
      }
      else {
        // has expired
        MessageBoxes.createOk().withHeader(ScoutTexts.get("PasswordHasExpiredTitle")).withBody(ScoutTexts.get("PasswordHasExpiredHeader")).show();
        changeNow = true;
      }
      //
      if (changeNow) {
        callPasswordForm(userId);
        // re-check
        expiryDate = service.getPasswordExpirationDate(userId);
      }
      return expiryDate.after(now);
    }
    catch (Exception t) {
      BEANS.get(ExceptionHandler.class).handle(t);
      return false;
    }
  }

  protected void callPasswordForm(String userId) {
    DefaultPasswordForm form = new DefaultPasswordForm();
    form.setUserId(userId);
    form.startChange();
    form.waitFor();
  }

}
