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
package org.eclipse.scout.rt.client.ui.form.fields.mailfield;

import java.io.File;
import java.net.URL;

import javax.mail.internet.MimeMessage;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * @version 3.x
 */
public abstract class AbstractMailField extends AbstractValueField<MimeMessage> implements IMailField {
  private IMailFieldUIFacade m_uiFacade;
  private boolean m_mailEditor;
  private boolean m_scrollBarEnabled;

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMailEditor() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(260)
  @ConfigPropertyValue("\"From\"")
  protected String getConfiguredLabelFrom() {
    return ScoutTexts.get("EmailFrom");
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(270)
  @ConfigPropertyValue("\"To\"")
  protected String getConfiguredLabelTo() {
    return ScoutTexts.get("EmailTo");
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(280)
  @ConfigPropertyValue("\"CC\"")
  protected String getConfiguredLabelCc() {
    return ScoutTexts.get("EmailCc");
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(290)
  @ConfigPropertyValue("\"Subject\"")
  protected String getConfiguredLabelSubject() {
    return ScoutTexts.get("EmailSubject");
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(300)
  @ConfigPropertyValue("\"Sent\"")
  protected String getConfiguredLabelSent() {
    return ScoutTexts.get("EmailSent");
  }

  /**
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execAttachementAction(File file) throws ProcessingException {
  }

  /**
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(240)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    m_mailEditor = getConfiguredMailEditor();
    m_scrollBarEnabled = getConfiguredScrollBarEnabled();
    setLabelFrom(getConfiguredLabelFrom());
    setLabelTo(getConfiguredLabelTo());
    setLabelCc(getConfiguredLabelCc());
    setLabelSent(getConfiguredLabelSent());
    setLabelSubject(getConfiguredLabelSubject());
  }

  public void setLabelFrom(String fromLabel) {
    propertySupport.setPropertyString(PROP_LABEL_FROM, fromLabel);
  }

  public String getLabelFrom() {
    return propertySupport.getPropertyString(PROP_LABEL_FROM);
  }

  public void setLabelTo(String toLabel) {
    propertySupport.setPropertyString(PROP_LABEL_TO, toLabel);
  }

  public String getLabelTo() {
    return propertySupport.getPropertyString(PROP_LABEL_TO);
  }

  public void setLabelCc(String ccLabel) {
    propertySupport.setPropertyString(PROP_LABEL_CC, ccLabel);
  }

  public String getLabelCc() {
    return propertySupport.getPropertyString(PROP_LABEL_CC);
  }

  public void setLabelSubject(String subjectLabel) {
    propertySupport.setPropertyString(PROP_LABEL_SUBJECT, subjectLabel);

  }

  public String getLabelSubject() {
    return propertySupport.getPropertyString(PROP_LABEL_SUBJECT);
  }

  public void setLabelSent(String sentLabel) {
    propertySupport.setProperty(PROP_LABEL_SENT, sentLabel);
  }

  public String getLabelSent() {
    return propertySupport.getPropertyString(PROP_LABEL_SENT);
  }

  public IMailFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  public boolean isMailEditor() {
    return m_mailEditor;
  }

  public boolean isScrollBarEnabled() {
    return m_scrollBarEnabled;
  }

  public void doAttachementAction(File file) throws ProcessingException {
    execAttachementAction(file);
  }

  public void doHyperlinkAction(URL url) throws ProcessingException {
    execHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals("local"));
  }

  private class P_UIFacade implements IMailFieldUIFacade {
    public boolean setMailFromUI(MimeMessage message) {
      if (isMailEditor()) {
        setValue(message);
      }
      return true;
    }

    public void fireAttachementActionFromUI(File file) {
      try {
        doAttachementAction(file);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

    public void fireHyperlinkActionFromUI(URL url) {
      try {
        doHyperlinkAction(url);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

  }

}
