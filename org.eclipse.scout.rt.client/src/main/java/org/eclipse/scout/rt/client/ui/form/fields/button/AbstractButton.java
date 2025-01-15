/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonClickActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonSelectionChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.IButtonExtension;
import org.eclipse.scout.rt.client.res.AttachmentSupport;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

@ClassId("998788cf-df0f-480b-bd5a-5037805610c9")
public abstract class AbstractButton extends AbstractFormField implements IButton {
  private final FastListenerList<ButtonListener> m_listenerList;
  private final IButtonUIFacade m_uiFacade;
  private final OptimisticLock m_uiFacadeSetSelectedLock;

  private int m_systemType;
  private int m_displayStyle;
  private boolean m_processButton;
  private AttachmentSupport m_attachmentSupport;

  public AbstractButton() {
    this(true);
  }

  public AbstractButton(boolean callInitializer) {
    super(false);
    m_listenerList = new FastListenerList<>();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_uiFacadeSetSelectedLock = new OptimisticLock();
    m_attachmentSupport = BEANS.get(AttachmentSupport.class);
    if (callInitializer) {
      callInitializer();
    }
  }

  /*
   * Configuration
   */

  /**
   * Configures the system type of this button. See {@code IButton.SYSTEM_TYPE_* } constants for valid values. System
   * buttons are buttons with pre-defined behavior (such as an 'Ok' button or a 'Cancel' button).
   * <p>
   * Subclasses can override this method. Default is {@code IButton.SYSTEM_TYPE_NONE}.
   *
   * @return the system type for a system button, or {@code IButton.SYSTEM_TYPE_NONE} for a non-system button
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BUTTON_SYSTEM_TYPE)
  @Order(200)
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_NONE;
  }

  /**
   * Configures whether this button is a process button. Process buttons are typically displayed on a dedicated button
   * bar at the bottom of a form. Non-process buttons can be placed anywhere on a form.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this button is a process button, {@code false} otherwise
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(220)
  protected boolean getConfiguredProcessButton() {
    return true;
  }

  /**
   * Configures whether this button is a default button. Default buttons typically have a dedicated look.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return {@code Boolean.TRUE} if this button is a default button, {@code Boolean.FALSE} if this button is never a
   * default button and {@code null} if the button look should be calculated with respect to
   * {@link AbstractButton#getConfiguredKeyStroke()}
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(225)
  protected Boolean getConfiguredDefaultButton() {
    return null;
  }

  /**
   * Use IKeyStroke constants to define a key stroke for Button execution.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  protected String getConfiguredKeyStroke() {
    return null;
  }

  /**
   * Configures the scope where the keystroke of this button is registered. If nothing is configured the Keystroke is
   * set on the form.
   */
  @Order(240)
  protected Class<? extends IFormField> getConfiguredKeyStrokeScopeClass() {
    return null;
  }

  /**
   * Configures the display style of this button. See {@code IButton.DISPLAY_STYLE_* } constants for valid values.
   * <p>
   * Subclasses can override this method. Default is {@code IButton.DISPLAY_STYLE_DEFAULT}.
   *
   * @return the display style of this button
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BUTTON_DISPLAY_STYLE)
  @Order(210)
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * {@inheritDoc} Default for buttons is false because they usually should only take as much place as is needed to
   * display the button label, but not necessarily more.
   */
  @Override
  protected boolean getConfiguredFillHorizontal() {
    return false;
  }

  /**
   * {@inheritDoc} Default for buttons is false because they usually should use the whole width set by grid layout.
   */
  @Override
  protected boolean getConfiguredGridUseUiWidth() {
    return false;
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return false;
  }

  /**
   * Configures the icon for this button. The icon is displayed on the button itself. Depending on UI and look and feel,
   * this button might support icon groups to represent different states of this button, such as enabled, disabled,
   * mouse-over etc.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(190)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * Configures whether or not the space for the status is visible.
   * <p>
   * Default for buttons is false, because they normally don't fill the grid cell and therefore it is not necessary to
   * align their status with the status of other fields. This makes sure the space next to the button is not wasted by
   * an invisible status.
   */
  @Override
  protected boolean getConfiguredStatusVisible() {
    return false;
  }

  /**
   * Configures whether two or more consecutive clicks on the button within a short period of time (e.g. double click)
   * should be prevented by the UI.
   * <p>
   * The default is <code>false</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredPreventDoubleClick() {
    return false;
  }

  /**
   * Configures if the button is stackable. A stackable button will be stacked in a dropdown menu if there is not enough
   * space in the menubar.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(210)
  protected boolean getConfiguredStackable() {
    return true;
  }

  /**
   * Configures if the button is shrinkable. A shrinkable button will be displayed without label but only with its
   * configured icon if there is not enough space in the menubar.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(220)
  protected boolean getConfiguredShrinkable() {
    return false;
  }

  /**
   * Called whenever this button is clicked. This button is disabled and cannot be clicked again until this method
   * returns.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(190)
  protected void execClickAction() {
  }

  /**
   * Called whenever the selection (of toggle-button) is changed.
   *
   * @param selection
   *     the new selection state
   */
  @ConfigOperation
  @Order(210)
  protected void execSelectionChanged(boolean selection) {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    int systemType = getConfiguredSystemType();
    setSystemType(systemType);
    setInheritAccessibility(getConfiguredInheritAccessibility() && systemType != SYSTEM_TYPE_CANCEL && systemType != SYSTEM_TYPE_CLOSE);
    setDisplayStyleInternal(getConfiguredDisplayStyle());
    setProcessButton(getConfiguredProcessButton());
    setDefaultButton(getConfiguredDefaultButton());
    setIconId(getConfiguredIconId());
    setKeyStroke(getConfiguredKeyStroke());
    setKeyStrokeScopeClass(getConfiguredKeyStrokeScopeClass());
    setPreventDoubleClick(getConfiguredPreventDoubleClick());
    setStackable(getConfiguredStackable());
    setShrinkable(getConfiguredShrinkable());
  }

  /*
   * Properties
   */
  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @SuppressWarnings("unchecked")
  private Class<? extends IFormField> getKeyStrokeScopeClass() {
    return (Class) propertySupport.getProperty(PROP_KEY_STROKE_SCOPE_CLASS);
  }

  @Override
  public IFormField getKeyStrokeScope() {
    if (getKeyStrokeScopeClass() != null) {
      return getForm().getFieldByClass(getKeyStrokeScopeClass());
    }
    return null;
  }

  private void setKeyStrokeScopeClass(Class<? extends IFormField> scope) {
    propertySupport.setProperty(PROP_KEY_STROKE_SCOPE_CLASS, scope);
  }

  @Override
  public String getKeyStroke() {
    return propertySupport.getPropertyString(PROP_KEY_STROKE);
  }

  @Override
  public void setKeyStroke(String keyStroke) {
    propertySupport.setPropertyString(PROP_KEY_STROKE, keyStroke);
  }

  @Override
  public int getSystemType() {
    return m_systemType;
  }

  @Override
  public void setSystemType(int systemType) {
    m_systemType = systemType;
  }

  @Override
  public boolean isProcessButton() {
    return m_processButton;
  }

  @Override
  public void setProcessButton(boolean on) {
    m_processButton = on;
  }

  @Override
  public Boolean getDefaultButton() {
    return (Boolean) propertySupport.getProperty(PROP_DEFAULT_BUTTON);
  }

  @Override
  public void setDefaultButton(Boolean defaultButton) {
    propertySupport.setProperty(PROP_DEFAULT_BUTTON, defaultButton);
  }

  @Override
  public void doClick() {
    if (isEnabled() && isVisible()) {
      fireButtonClicked();
      interceptClickAction();
    }
  }

  /**
   * Toggle and Radio Buttons
   */
  @Override
  public boolean isSelected() {
    return propertySupport.getPropertyBool(PROP_SELECTED);
  }

  /**
   * Toggle and Radio Buttons
   */
  @Override
  public void setSelected(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_SELECTED, b);
    // single observer for config
    if (changed) {
      try {
        interceptSelectionChanged(b);
      }
      catch (RuntimeException | PlatformError e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  @Override
  public boolean isPreventDoubleClick() {
    return propertySupport.getPropertyBool(PROP_PREVENT_DOUBLE_CLICK);
  }

  @Override
  public void setPreventDoubleClick(boolean preventDoubleClick) {
    propertySupport.setPropertyBool(PROP_PREVENT_DOUBLE_CLICK, preventDoubleClick);
  }

  @Override
  public boolean isStackable() {
    return propertySupport.getPropertyBool(PROP_STACKABLE);
  }

  @Override
  public void setStackable(boolean stackable) {
    propertySupport.setPropertyBool(PROP_STACKABLE, stackable);
  }

  @Override
  public boolean isShrinkable() {
    return propertySupport.getPropertyBool(PROP_SHRINKABLE);
  }

  @Override
  public void setShrinkable(boolean shrinkable) {
    propertySupport.setPropertyBool(PROP_SHRINKABLE, shrinkable);
  }

  @Override
  public void requestPopup() {
    fireRequestPopup();
  }

  @Override
  public int getDisplayStyle() {
    return m_displayStyle;
  }

  @Override
  public void setDisplayStyleInternal(int i) {
    m_displayStyle = i;
  }

  @Override
  public IButtonUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public IFastListenerList<ButtonListener> buttonListeners() {
    return m_listenerList;
  }

  private void fireButtonClicked() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_CLICKED));
  }

  private void fireRequestPopup() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_REQUEST_POPUP));
  }

  // main handler
  private void fireButtonEvent(ButtonEvent e) {
    buttonListeners().list().forEach(listener -> listener.buttonChanged(e));
  }

  @Override
  public void setView(boolean visible, boolean enabled) {
    setVisible(visible);
    setEnabled(enabled);
  }

  /**
   * local images and local resources bound to the html text
   */
  @Override
  public Set<BinaryResource> getAttachments() {
    return m_attachmentSupport.getAttachments();
  }

  @Override
  public BinaryResource getAttachment(String filename) {
    return m_attachmentSupport.getAttachment(filename);
  }

  @Override
  public void setAttachments(Collection<? extends BinaryResource> attachments) {
    m_attachmentSupport.setAttachments(attachments);
  }

  /**
   * Default implementation for buttons
   */
  protected class P_UIFacade implements IButtonUIFacade {
    @Override
    public void fireButtonClickFromUI() {
      if (isEnabledIncludingParents() && isVisibleIncludingParents()) {
        doClick();
      }
    }

    /**
     * Toggle and Radio Buttons
     */
    @Override
    public void setSelectedFromUI(boolean b) {
      // Ticket 76711: added optimistic lock
      if (isEnabled() && isVisible() && m_uiFacadeSetSelectedLock.acquire()) {
        try {
          setSelected(b);
        }
        finally {
          m_uiFacadeSetSelectedLock.release();
        }
      }
    }
  }

  protected final void interceptSelectionChanged(boolean selection) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ButtonSelectionChangedChain chain = new ButtonSelectionChangedChain(extensions);
    chain.execSelectionChanged(selection);
  }

  protected final void interceptClickAction() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ButtonClickActionChain chain = new ButtonClickActionChain(extensions);
    chain.execClickAction();
  }

  protected static class LocalButtonExtension<OWNER extends AbstractButton> extends LocalFormFieldExtension<OWNER> implements IButtonExtension<OWNER> {

    public LocalButtonExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execSelectionChanged(ButtonSelectionChangedChain chain, boolean selection) {
      getOwner().execSelectionChanged(selection);
    }

    @Override
    public void execClickAction(ButtonClickActionChain chain) {
      getOwner().execClickAction();
    }
  }

  @Override
  protected IButtonExtension<? extends AbstractButton> createLocalExtension() {
    return new LocalButtonExtension<>(this);
  }
}
