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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.useradmin.DefaultPasswordForm;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.pwd.IPasswordManagementService;
import org.eclipse.scout.service.SERVICES;

/**
 * Runs the password verification process
 */
public class PasswordPolicyVerifier {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PasswordPolicyVerifier.class);

  /**
   * Calls {@link IPasswordManagementService#getPasswordExpirationDate(String)} to check whether the password has
   * expired. When desired, warns the user in
   * advance about the expiration. If expired, calls the {@link DefaultPasswordForm#startChange()} and - when closed -
   * re-checks the
   * expiry date. When still expired, exits the application (scout session).
   * 
   * @param warnInAdvanceDays
   *          number of days before the expiry when a warning shall occur, -1 to
   *          omit this feature
   * @return true if the password is not expired, false if - after all - the
   *         password has expired. Normally when returned false, the application
   *         quits.
   */
  public boolean verify(String userId, int warnInAdvanceDays) {
    IPasswordManagementService service = SERVICES.getService(IPasswordManagementService.class);
    if (service == null) {
      LOG.error("missing client service proxy for " + IPasswordManagementService.class.getName() + "; check plugin extensions");
      return false;
    }
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
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
          int answer = MessageBox.showYesNoCancelMessage(
              ScoutTexts.get("PasswordWillExpireTitle"),
              remainDays == 0 ?
                  ScoutTexts.get("PasswordWillExpireHeaderX", ScoutTexts.get("Today")) :
                  remainDays == 1 ?
                      ScoutTexts.get("PasswordWillExpireHeaderX", ScoutTexts.get("Tomorrow")) :
                      ScoutTexts.get("PasswordWillExpireHeaderX", ScoutTexts.get("InDaysX", "" + remainDays)),
              ScoutTexts.get("PasswordWillExpireInfo")
              );
          if (answer == MessageBox.YES_OPTION) {
            changeNow = true;
          }
        }
      }
      else {
        // has expired
        MessageBox.showOkMessage(ScoutTexts.get("PasswordHasExpiredTitle"), ScoutTexts.get("PasswordHasExpiredHeader"), null);
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
    catch (Throwable t) {
      ProcessingException pe = (t instanceof ProcessingException ? (ProcessingException) t : new ProcessingException("Unexpected", t));
      SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
      return false;
    }
  }

  protected void callPasswordForm(String userId) throws ProcessingException {
    DefaultPasswordForm form = new DefaultPasswordForm();
    form.setUserId(userId);
    form.startChange();
    form.waitFor();
  }

}
