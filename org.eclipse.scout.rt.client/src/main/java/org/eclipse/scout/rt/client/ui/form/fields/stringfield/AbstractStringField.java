/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.IStringFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldDropRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.StringFieldChains.StringFieldLinkActionChain;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("d8b1f73a-4415-4477-8408-e6ada9e69551")
public abstract class AbstractStringField extends AbstractBasicField<String> implements IStringField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractStringField.class);

  private IStringFieldUIFacade m_uiFacade;
  private boolean m_enabledProcessing;

  public AbstractStringField() {
    this(true);
  }

  public AbstractStringField(boolean callInitializer) {
    super(callInitializer);
    m_enabledProcessing = true;
  }

  @Override
  protected IStringFieldExtension<? extends AbstractStringField> createLocalExtension() {
    return new LocalStringFieldExtension<>(this);
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  protected boolean getConfiguredHasAction() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(240)
  protected String getConfiguredFormat() {
    return null;
  }

  /**
   * @return true if all characters should be masked (e.g. for a password field). default is false.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredInputMasked() {
    return false;
  }

  /**
   * @return true if all characters should be transformed to lower case when typing text into the string field. default
   *         is false.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredFormatLower() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(290)
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(230)
  protected boolean getConfiguredMultilineText() {
    return false;
  }

  /**
   * @return true if all characters should be transformed to upper case when typing text into the string field. default
   *         is false.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  protected boolean getConfiguredFormatUpper() {
    return false;
  }

  /**
   * @return true if leading and trailing whitespace should be stripped from the entered text while validating the
   *         value. default is true.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  protected boolean getConfiguredTrimText() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(320)
  protected boolean getConfiguredWrapText() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(330)
  protected String getConfiguredValueFormat() {
    return null;
  }

  /**
   * Define whether selection tracking should be enabled (might increase number of events between client and fronted
   * server).
   * <p>
   * If <code>false</code>, {@link #getSelectionStart()} and {@link #getSelectionEnd()} might not reflect the actual
   * selection.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(340)
  protected boolean getConfiguredSelectionTrackingEnabled() {
    return false;
  }

  /**
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(190)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
  }

  /**
   * Configures the drop support of this string field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(400)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * <p>
   * Configures the drag support of this string field.
   * </p>
   * <p>
   * Method marked as final as currently only drop is implemented for this field.
   * </p>
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(410)
  protected final int getConfiguredDragType() {
    return 0;
  }

  @ConfigOperation
  @Order(500)
  protected final TransferObject execDragRequest() {
    return null;
  }

  @ConfigOperation
  @Order(510)
  protected void execDropRequest(TransferObject transferObject) {
  }

  /**
   * When a field is marked as link this method will be called. Implement it to add a link action behaviour.
   */
  @ConfigOperation
  @Order(240)
  protected void execAction() {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    setMaxLength(getConfiguredMaxLength());
    setInputMasked(getConfiguredInputMasked());
    super.initConfig();
    if (getConfiguredFormatLower()) {
      setFormatLower();
    }
    else if (getConfiguredFormatUpper()) {
      setFormatUpper();
    }
    else {
      setFormat(getConfiguredFormat());
    }
    setHasAction(getConfiguredHasAction());
    setWrapText(getConfiguredWrapText());
    setTrimText(getConfiguredTrimText());
    setMultilineText(getConfiguredMultilineText());
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
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setSelectionTrackingEnabled(getConfiguredSelectionTrackingEnabled());
    setSpellCheckEnabled(computeSpellCheckEnabled());
  }

  @Override
  public void setMaxLength(int maxLength) {
    boolean changed = propertySupport.setPropertyInt(PROP_MAX_LENGTH, Math.max(0, maxLength));
    if (changed && isInitConfigDone()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  protected String validateValueInternal(String rawValue) {
    String validValue = super.validateValueInternal(rawValue);

    if (validValue != null) {
      if (isTrimText()) {
        validValue = validValue.trim();
      }
      if (validValue.length() > getMaxLength()) {
        validValue = validValue.substring(0, getMaxLength());
        if (isTrimText()) { // trim again
          validValue = validValue.trim();
        }
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
    return StringUtility.nullIfEmpty(validValue);
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
  public void setHasAction(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_ACTION, b);
  }

  @Override
  public boolean isHasAction() {
    return propertySupport.getPropertyBool(PROP_HAS_ACTION);
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
  public void setTrimText(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE, b);
    if (b & changed && isInitConfigDone()) {
      setValue(getValue());
    }
  }

  @Override
  public boolean isTrimText() {
    return propertySupport.getPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE);
  }

  @Override
  public void setMultilineText(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, b);
    if (!b & changed && isInitConfigDone()) {
      setValue(getValue());
    }
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

  /**
   * Use {@link #getConfiguredSelectionTrackingEnabled()}, {@link #isSelectionTrackingEnabled()} and
   * {@link #setSelectionTrackingEnabled(boolean)} to enable selection tracking. If
   * {@link #isSelectionTrackingEnabled()} is not <code>true</code> the return value of this method might not reflect
   * the actual selection start.
   */
  @Override
  public int getSelectionStart() {
    return propertySupport.getPropertyInt(PROP_SELECTION_START);
  }

  /**
   * Use {@link #getConfiguredSelectionTrackingEnabled()}, {@link #isSelectionTrackingEnabled()} and
   * {@link #setSelectionTrackingEnabled(boolean)} to enable selection tracking. If
   * {@link #isSelectionTrackingEnabled()} is not <code>true</code> the return value of this method might not reflect
   * the actual selection end.
   */
  @Override
  public int getSelectionEnd() {
    return propertySupport.getPropertyInt(PROP_SELECTION_END);
  }

  /**
   * @see {@link #getConfiguredSelectionTrackingEnabled()}
   */
  @Override
  public boolean isSelectionTrackingEnabled() {
    return propertySupport.getPropertyBool(PROP_SELECTION_TRACKING_ENABLED);
  }

  /**
   * @see {@link #getConfiguredSelectionTrackingEnabled()}
   */
  @Override
  public void setSelectionTrackingEnabled(boolean selectionTrackingEnabled) {
    propertySupport.setPropertyBool(PROP_SELECTION_TRACKING_ENABLED, selectionTrackingEnabled);
  }

  @Override
  public IStringFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setFormat(String s) {
    propertySupport.setPropertyString(PROP_FORMAT, s);
    refreshDisplayText();
  }

  @Override
  public String getFormat() {
    return propertySupport.getPropertyString(PROP_FORMAT);
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) {
    if (text != null && text.isEmpty()) {
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

  @Override
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(PROP_DROP_MAXIMUM_SIZE);
  }

  @Override
  public boolean isEnabledProcessing() {
    return m_enabledProcessing;
  }

  private void setEnabledProcessing(boolean b) {
    m_enabledProcessing = b;
  }

  @Override
  public void doAction() {
    if (isHasAction() && isEnabled() && isVisible() && isEnabledProcessing()) {
      try {
        setEnabledProcessing(false);
        interceptAction();
      }
      finally {
        setEnabledProcessing(true);
      }
    }
  }

  protected class P_UIFacade extends AbstractBasicField.P_UIFacade implements IStringFieldUIFacade {

    @Override
    public void fireActionFromUI() {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        return;
      }
      doAction();
    }

    @Override
    public void setSelectionFromUI(int startOfSelection, int endOfSelection) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        return;
      }
      select(startOfSelection, endOfSelection);
    }

    @Override
    public TransferObject fireDragRequestFromUI() {
      return interceptDragRequest();
    }

    @Override
    public void fireDropActionFromUi(TransferObject scoutTransferable) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        //do not drop into disabled ore invisible fields.
        return;
      }
      interceptDropRequest(scoutTransferable);
    }
  }

  /**
   * Compute whether this field is spell checkable (based on several field settings).
   * <p>
   * Method called by {@link AbstractStringField#initConfig()} to set an initial value for
   * {@link AbstractStringField#setSpellCheckEnabled(boolean)}.
   */
  protected boolean computeSpellCheckEnabled() {
    return !this.isHasAction()
        && !this.isFormatUpper()
        && !this.isFormatLower()
        && this.isMultilineText();
  }

  @Override
  public void setSpellCheckEnabled(boolean spellCheckEnabled) {
    propertySupport.setPropertyBool(PROP_SPELL_CHECK_ENABLED, spellCheckEnabled);
  }

  @Override
  public boolean isSpellCheckEnabled() {
    return propertySupport.getPropertyBool(PROP_SPELL_CHECK_ENABLED);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalStringFieldExtension<OWNER_FIELD extends AbstractStringField> extends LocalBasicFieldExtension<String, OWNER_FIELD>
      implements IStringFieldExtension<OWNER_FIELD> {

    public LocalStringFieldExtension(OWNER_FIELD owner) {
      super(owner);
    }

    @Override
    public void execDropRequest(StringFieldDropRequestChain chain, TransferObject transferObject) {
      getOwner().execDropRequest(transferObject);
    }

    @Override
    public void execAction(StringFieldLinkActionChain chain) {
      getOwner().execAction();
    }

    @Override
    public TransferObject execDragRequest(StringFieldDragRequestChain chain) {
      return getOwner().execDragRequest();
    }

  }

  protected final void interceptDropRequest(TransferObject transferObject) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    StringFieldDropRequestChain chain = new StringFieldDropRequestChain(extensions);
    chain.execDropRequest(transferObject);
  }

  protected final void interceptAction() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    StringFieldLinkActionChain chain = new StringFieldLinkActionChain(extensions);
    chain.execAction();
  }

  protected final TransferObject interceptDragRequest() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    StringFieldDragRequestChain chain = new StringFieldDragRequestChain(extensions);
    return chain.execDragRequest();
  }

}
