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
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import java.net.URL;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

public abstract class AbstractStringField extends AbstractValueField<String> implements IStringField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractStringField.class);

  private IStringFieldUIFacade m_uiFacade;
  private Boolean m_monitorSpelling = null; // If null the application-wide

  // default is used

  public AbstractStringField() {
    this(true);
  }

  public AbstractStringField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredDecorationLink() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(240)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredInputMasked() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredFormatLower() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(290)
  @ConfigPropertyValue("4000")
  @ValidationRule(ValidationRule.MAX_LENGTH)
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(230)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMultilineText() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredFormatUpper() {
    return false;
  }

  /**
   * Causes the ui to send a validate event every time the text field content is changed.
   * <p>
   * Be careful when using this property since this can influence performance and the charateristics of text input.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredValidateOnAnyKey() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(320)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredWrapText() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(330)
  @ConfigPropertyValue("null")
  protected String getConfiguredValueFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(340)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredSelectAllOnFocus() {
    return true;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(400)
  @ConfigPropertyValue("0")
  protected int getConfiguredDropType() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(410)
  @ConfigPropertyValue("0")
  protected int getConfiguredDragType() {
    return 0;
  }

  @ConfigOperation
  @Order(500)
  protected TransferObject execDragRequest() {
    return null;
  }

  @ConfigOperation
  @Order(510)
  protected void execDropRequest(TransferObject transferObject) {
  }

  /**
   * When a link is marked as link this method will be called. Implement it to
   * add a link action behaviour.
   */
  @ConfigOperation
  @Order(240)
  protected void execLinkAction(URL url) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setMaxLength(getConfiguredMaxLength());
    setInputMasked(getConfiguredInputMasked());
    if (getConfiguredFormatLower()) {
      setFormatLower();
    }
    else if (getConfiguredFormatUpper()) {
      setFormatUpper();
    }
    else {
      setFormat(getConfiguredFormat());
    }
    setDecorationLink(getConfiguredDecorationLink());
    setWrapText(getConfiguredWrapText());
    setMultilineText(getConfiguredMultilineText());
    setSelectAllOnFocus(getConfiguredSelectAllOnFocus() && !getConfiguredMultilineText());
    setValidateOnAnyKey(getConfiguredValidateOnAnyKey());
    int configuredDragType = getConfiguredDragType();
    if (IDNDSupport.TYPE_TEXT_TRANSFER == configuredDragType) {
      LOG.warn("Drag and drop TextTransfer is default behaviour (Configuration will not be considered).");
      configuredDragType = 0;
    }
    setDragType(configuredDragType);
    int configuredDropType = getConfiguredDropType();
    if (IDNDSupport.TYPE_TEXT_TRANSFER == configuredDropType) {
      LOG.warn("Drag and drop TextTransfer is default behaviour (Configuration will not be considered).");
      configuredDropType = 0;
    }
    setDropType(configuredDropType);
  }

  @Override
  public void setMaxLength(int len) {
    if (len > 0) {
      propertySupport.setPropertyInt(PROP_MAX_LENGTH, len);
    }
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    int len = propertySupport.getPropertyInt(PROP_MAX_LENGTH);
    if (len <= 0) {
      len = 200;
    }
    return len;
  }

  @Override
  protected String validateValueInternal(String rawValue) throws ProcessingException {
    String validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    if (validValue != null && validValue.length() == 0) {
      validValue = null;
    }
    if (validValue != null) {
      if (validValue.length() > getMaxLength()) {
        validValue = validValue.substring(0, getMaxLength());
      }
      if (isFormatUpper()) {
        validValue = validValue.toUpperCase();
      }
      else if (isFormatLower()) {
        validValue = validValue.toLowerCase();
      }
    }
    /*
     * bsi ticket 95042
     */
    if (validValue != null && !isMultilineText()) {
      // omit leading and trailing newlines
      validValue = StringUtility.trimNewLines(validValue);
      // replace newlines by spaces
      validValue = validValue.replaceAll("\r\n", " ").replaceAll("[\r\n]", " ");
    }
    return validValue;
  }

  @Override
  public void setInputMasked(boolean b) {
    propertySupport.setPropertyBool(PROP_INPUT_MASKED, b);
  }

  @Override
  public boolean isInputMasked() {
    return propertySupport.getPropertyBool(PROP_INPUT_MASKED);
  }

  @Override
  public void setFormatUpper() {
    setFormat(FORMAT_UPPER);
  }

  @Override
  public boolean isFormatUpper() {
    return FORMAT_UPPER.equals(getFormat());
  }

  @Override
  public void setFormatLower() {
    setFormat(FORMAT_LOWER);
  }

  @Override
  public boolean isFormatLower() {
    return FORMAT_LOWER.equals(getFormat());
  }

  @Override
  public void setDecorationLink(boolean b) {
    propertySupport.setPropertyBool(PROP_DECORATION_LINK, b);
  }

  @Override
  public boolean isDecorationLink() {
    return propertySupport.getPropertyBool(PROP_DECORATION_LINK);
  }

  @Override
  public void setWrapText(boolean b) {
    propertySupport.setPropertyBool(PROP_WRAP_TEXT, b);
  }

  @Override
  public boolean isWrapText() {
    return propertySupport.getPropertyBool(PROP_WRAP_TEXT);
  }

  @Override
  public void setMultilineText(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, b);
  }

  @Override
  public boolean isMultilineText() {
    return propertySupport.getPropertyBool(PROP_MULTILINE_TEXT);
  }

  @Override
  public void insertText(String s) {
    propertySupport.setPropertyAlwaysFire(PROP_INSERT_TEXT, s);
  }

  @Override
  public boolean isSelectAllOnFocus() {
    return propertySupport.getPropertyBool(PROP_SELECT_ALL_ON_FOCUS);
  }

  @Override
  public void setSelectAllOnFocus(boolean b) {
    propertySupport.setPropertyBool(PROP_SELECT_ALL_ON_FOCUS, b);
  }

  @Override
  public void setValidateOnAnyKey(boolean b) {
    propertySupport.setPropertyBool(PROP_VALIDATE_ON_ANY_KEY, b);
  }

  @Override
  public boolean isValidateOnAnyKey() {
    return propertySupport.getPropertyBool(PROP_VALIDATE_ON_ANY_KEY);
  }

  @Override
  public void select(int startIndex, int endIndex) {
    try {
      propertySupport.setPropertiesChanging(true);
      //
      propertySupport.setPropertyInt(PROP_SELECTION_START, startIndex);
      propertySupport.setPropertyInt(PROP_SELECTION_END, endIndex);
    }
    finally {
      propertySupport.setPropertiesChanging(false);
    }
  }

  @Override
  public int getSelectionStart() {
    return propertySupport.getPropertyInt(PROP_SELECTION_START);
  }

  @Override
  public int getSelectionEnd() {
    return propertySupport.getPropertyInt(PROP_SELECTION_END);
  }

  @Override
  public IStringFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setFormat(String s) {
    propertySupport.setPropertyString(PROP_FORMAT, s);
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        String t = execFormatValue(getValue());
        setDisplayText(t);
      }
    }
  }

  @Override
  public String getFormat() {
    return propertySupport.getPropertyString(PROP_FORMAT);
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    String fmt = getFormat();
    if (fmt != null && text != null) {
      if (IStringField.FORMAT_LOWER.equals(fmt)) {
        text = text.toLowerCase();
      }
      else if (IStringField.FORMAT_UPPER.equals(fmt)) {
        text = text.toUpperCase();
      }
    }
    return text;
  }

  // DND
  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  private class P_UIFacade implements IStringFieldUIFacade {

    @Override
    public boolean setTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      return parseValue(newText);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringFieldUIFacade
     * #fireLinkActionFromUI(java.lang.String)
     */
    @Override
    public void fireLinkActionFromUI(String text) {
      URL url = IOUtility.urlTextToUrl(text);
      try {
        execLinkAction(url);
      }
      catch (ProcessingException e) {
        LOG.warn("execLinkAction failed", e);
      }
    }

    @Override
    public void fireKeyTypedFromUI(String newText) {
      String oldText = getDisplayText();
      if (oldText != null && oldText.length() == 0) {
        oldText = null;
      }
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      if (oldText == newText || (oldText != null && oldText.equals(newText))) {
        // no change
        return;
      }
      parseValue(newText);
    }

    @Override
    public void setSelectionFromUI(int startOfSelection, int endOfSelection) {
      select(startOfSelection, endOfSelection);
    }

    @Override
    public TransferObject fireDragRequestFromUI() {
      return execDragRequest();
    }

    @Override
    public void fireDropActionFromUi(TransferObject scoutTransferable) {
      execDropRequest(scoutTransferable);
    }
  }

  /**
   * Returns whether this text component is spell checkable.
   */
  @Override
  public boolean isSpellCheckEnabled() {
    return (!this.isDecorationLink() &&
        !this.isFormatUpper() &&
        !this.isFormatLower() &&
        this.isEnabled() &&
        this.isEnabledGranted() && (!(this.getForm() instanceof ISearchForm)));
  }

  /**
   * Returns whether this text component should be monitored for spelling errors
   * in the background ("check as you type").<br>
   * If it is not defined, null is returned, then the application default is
   * used.
   */
  @Override
  public Boolean isSpellCheckAsYouTypeEnabled() {
    return m_monitorSpelling;
  }

  /**
   * Sets whether to monitor this text component for spelling errors in the
   * background ("check as you type").<br>
   * Use null for application default.
   */
  @Override
  public void setSpellCheckAsYouTypeEnabled(boolean monitorSpelling) {
    m_monitorSpelling = new Boolean(monitorSpelling);
  }

}
