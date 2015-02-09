/*******************************************************************************
 * Copyright (c) 2010,2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action;

import java.security.Permission;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionInitActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionSelectionChangedChain;
import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStrokeNormalizer;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractAction extends AbstractPropertyObserver implements IAction, IExtensibleObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractAction.class);

  private boolean m_initialized;
  private final IActionUIFacade m_uiFacade;
  private boolean m_inheritAccessibility;
  // enabled is defined as: enabledGranted && enabledProperty && enabledProcessing && enabledInheritAccessibility
  private boolean m_enabledGranted;
  private boolean m_enabledProperty;
  private boolean m_enabledProcessingAction;
  private boolean m_enabledInheritAccessibility;
  private boolean m_visibleProperty;
  private boolean m_visibleGranted;
  private boolean m_toggleAction;

  private boolean m_separator;
  private final ObjectExtensions<AbstractAction, IActionExtension<? extends AbstractAction>> m_objectExtensions;

  public AbstractAction() {
    this(true);
  }

  public AbstractAction(boolean callInitializer) {
    m_uiFacade = createUIFacade();
    m_enabledGranted = true;
    m_enabledProcessingAction = true;
    m_enabledInheritAccessibility = true;
    m_visibleGranted = true;
    m_objectExtensions = new ObjectExtensions<AbstractAction, IActionExtension<? extends AbstractAction>>(this);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  /**
   * This is the init of the runtime model after the environment (form, fields, ..) are built
   * and configured
   */
  @Override
  public final void initAction() throws ProcessingException {
    interceptInitAction();
  }

  /*
   * Configuration
   */
  /**
   * Configures the icon for this action.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(30)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(40)
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredTooltipText() {
    return null;
  }

  /**
   * Defines the keystroke for this action. A keystroke is built from optional modifiers (alt, control, shift)
   * and a key (p, f11, delete).
   * The keystroke has to follow a certain pattern: The modifiers (alt, shift, control) are separated from the key by a
   * '-'. Examples:
   * <ul>
   * <li>control-alt-1 (combineKeyStrokes(IKeyStroke.CONTROL,IKeyStroke.ALT,"1"))
   * <li>control-shift-alt-1 (combineKeyStrokes(IKeyStroke.CONTROL,IKeyStroke.SHIFT,IKeyStroke.ALT,"1"))
   * <li>f11 (IKeyStroke.F11)
   * <li>alt-f11 (combineKeyStrokes(IKeyStroke.ALT,IKeyStroke.F11))
   * </ul>
   *
   * @return
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(55)
  protected String getConfiguredKeyStroke() {
    return null;
  }

  /**
   * Configures whether the action can be selected or not
   *
   * @return <code>true</code> if the action can be selected and <code>false</code> otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  /**
   * Configures whether the action is visible or not
   *
   * @return <code>true</code> if the action is visible and <code>false</code> otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * @return true if {@link #prepareAction()} should in addition consider the
   *         context of the action to decide for visibility and enabled.<br>
   *         For example a menu of a table field with {@link #isInheritAccessibility()}==true is invisible when the
   *         table
   *         field is disabled or invisible
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(22)
  protected boolean getConfiguredInheritAccessibility() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(25)
  protected boolean getConfiguredToggleAction() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredSeparator() {
    return false;
  }

  /**
   * Configures the view order of this action. The view order determines the order in which the action appears.<br>
   * The view order of actions with no view order configured ({@code < 0}) is initialized based on the {@link Order}
   * annotation of the class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this action.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(120)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * called by {@link #initAction()}<br>
   * this way a menu can for example add/remove custom child menus
   */
  @ConfigOperation
  @Order(10)
  protected void execInitAction() throws ProcessingException {
  }

  /**
   * called by prepareAction before action is added to list or used<br>
   * this way a menu can be made dynamically visible / enabled
   *
   * @deprecated use {@link AbstractMenu#execOwnerValueChanged}
   */
  @Deprecated
  @Order(20)
  protected void execPrepareAction() throws ProcessingException {
  }

  /**
   * called when action is performed independent of the selection state.
   */
  @ConfigOperation
  @Order(30)
  protected void execAction() throws ProcessingException {
  }

  /**
   * Called whenever the selection (of toggle-action) is changed.
   *
   * @param selection
   *          the new selection state
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(32)
  protected void execSelectionChanged(boolean selection) throws ProcessingException {
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    setIconId(getConfiguredIconId());
    setText(getConfiguredText());
    setTooltipText(getConfiguredTooltipText());
    setKeyStroke(getConfiguredKeyStroke());
    setInheritAccessibility(getConfiguredInheritAccessibility());
    setEnabled(getConfiguredEnabled());
    setVisible(getConfiguredVisible());
    setToggleAction(getConfiguredToggleAction());
    setSeparator(getConfiguredSeparator());
    setOrder(calculateViewOrder());
  }

  protected IActionUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  protected IActionExtension<? extends AbstractAction> createLocalExtension() {
    return new LocalActionExtension<AbstractAction>(this);
  }

  @Override
  public final List<? extends IActionExtension<? extends AbstractAction>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public int acceptVisitor(IActionVisitor visitor) {
    switch (visitor.visit(this)) {
      case IActionVisitor.CANCEL:
        return IActionVisitor.CANCEL;
      case IActionVisitor.CANCEL_SUBTREE:
        return IActionVisitor.CONTINUE;
      case IActionVisitor.CONTINUE_BRANCH:
        return IActionVisitor.CANCEL;
      default:
        return IActionVisitor.CONTINUE;
    }
  }

  /**
   * Calculates the actions's view order, e.g. if the @Order annotation is set to 30.0, the method will
   * return 30.0. If no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 4.0.1
   */
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      Class<?> cls = getClass();
      while (cls != null && IAction.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  @Override
  public Object getProperty(String name) {
    return propertySupport.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    propertySupport.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public String getActionId() {
    Class<?> c = getClass();
    while (c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    String s = c.getName();
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  @Override
  public void doAction() throws ProcessingException {
    if (isEnabled() && isVisible()) {
      try {
        setEnabledProcessingAction(false);
        doActionInternal();
      }
      finally {
        setEnabledProcessingAction(true);
      }
    }
  }

  /**
   * Please double check if implementing this method!
   * Consider using {@link #interceptAction()} instead. If no other option ensure super call when overriding this
   * method.
   *
   * @throws ProcessingException
   */
  protected void doActionInternal() throws ProcessingException {
    interceptAction();
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public String getText() {
    return propertySupport.getPropertyString(PROP_TEXT);
  }

  @Override
  public String getTextWithMnemonic() {
    return propertySupport.getPropertyString(PROP_TEXT_WITH_MNEMONIC);
  }

  @Override
  public void setText(String text) {
    if (text != null) {
      propertySupport.setPropertyString(PROP_TEXT, StringUtility.removeMnemonic(text));
      propertySupport.setPropertyString(PROP_TEXT_WITH_MNEMONIC, text);
      propertySupport.setProperty(PROP_MNEMONIC, StringUtility.getMnemonic(text));
    }
    else {
      propertySupport.setPropertyString(PROP_TEXT, null);
      propertySupport.setPropertyString(PROP_TEXT_WITH_MNEMONIC, null);
      propertySupport.setProperty(PROP_MNEMONIC, (char) 0x0);
    }
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_VIEW_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_VIEW_ORDER, order);
  }

  @Override
  public String getKeyStroke() {
    return propertySupport.getPropertyString(PROP_KEYSTROKE);
  }

  @Override
  public void setKeyStroke(String k) {
    KeyStrokeNormalizer scoutKeystroke = new KeyStrokeNormalizer(k);
    scoutKeystroke.normalize();
    if (scoutKeystroke.isValid()) {
      propertySupport.setPropertyString(PROP_KEYSTROKE, scoutKeystroke.getNormalizedKeystroke());
    }
    else {
      LOG.warn("Could not create keystroke '" + k + "' because it is invalid!");
    }
  }

  @Override
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public void setTooltipText(String text) {
    propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, text);
  }

  @Override
  public boolean isSeparator() {
    return m_separator;
  }

  @Override
  public void setSeparator(boolean b) {
    m_separator = b;
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public boolean isThisAndParentsEnabled() {
    if (!isEnabled()) {
      return false;
    }
    IAction temp = this;
    while (temp instanceof IActionNode) {
      temp = ((IActionNode) temp).getParent();
      if (temp == null) {
        return true;
      }
      if (!temp.isEnabled()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void setEnabled(boolean b) {
    m_enabledProperty = b;
    setEnabledInternal();
  }

  @Override
  public boolean isSelected() {
    return propertySupport.getPropertyBool(PROP_SELECTED);
  }

  @Override
  public void setSelected(boolean b) {
    if (setSelectedInternal(b)) {
      try {
        interceptSelectionChanged(b);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  protected boolean setSelectedInternal(boolean b) {
    return propertySupport.setPropertyBool(PROP_SELECTED, b);
  }

  @Override
  public boolean isToggleAction() {
    return m_toggleAction;
  }

  @Override
  public void setToggleAction(boolean b) {
    m_toggleAction = b;
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public boolean isThisAndParentsVisible() {
    if (!isVisible()) {
      return false;
    }
    IAction temp = this;
    while (temp instanceof IActionNode) {
      temp = ((IActionNode) temp).getParent();
      if (temp == null) {
        return true;
      }
      if (!temp.isVisible()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void setVisible(boolean b) {
    m_visibleProperty = b;
    setVisibleInternal();
  }

  @Override
  public boolean isInheritAccessibility() {
    return m_inheritAccessibility;
  }

  @Override
  public void setInheritAccessibility(boolean b) {
    m_inheritAccessibility = b;
  }

  @Override
  public void setEnabledInheritAccessibility(boolean b) {
    m_enabledInheritAccessibility = b;
    setEnabledInternal();
  }

  @Override
  public boolean isEnabledInheritAccessibility() {
    return m_enabledInheritAccessibility;
  }

  /**
   * Access control<br>
   * when false, overrides isEnabled with false
   */
  @Override
  public void setEnabledPermission(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setEnabledGranted(b);
  }

  @Override
  public boolean isEnabledGranted() {
    return m_enabledGranted;
  }

  /**
   * Access control<br>
   * when false, overrides isEnabled with false
   */
  @Override
  public void setEnabledGranted(boolean b) {
    m_enabledGranted = b;
    setEnabledInternal();
  }

  @Override
  public boolean isEnabledProcessingAction() {
    return m_enabledProcessingAction;
  }

  @Override
  public void setEnabledProcessingAction(boolean b) {
    m_enabledProcessingAction = b;
    setEnabledInternal();
  }

  private void setEnabledInternal() {
    propertySupport.setPropertyBool(PROP_ENABLED, m_enabledGranted && m_enabledProperty && m_enabledProcessingAction && m_enabledInheritAccessibility);
  }

  @Override
  public void setVisiblePermission(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  @Override
  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    setVisibleInternal();
  }

  private void setVisibleInternal() {
    propertySupport.setPropertyBool(PROP_VISIBLE, m_visibleGranted && m_visibleProperty);
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getContainer() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getContainer().classId();
    }
    return simpleClassId;
  }

  @Override
  public char getMnemonic() {
    Character c = (Character) propertySupport.getProperty(PROP_MNEMONIC);
    return c != null ? c.charValue() : 0x00;
  }

  /**
   * Combine a key stroke consisting of multiple keys.
   */
  public String combineKeyStrokes(String... keys) {
    StringBuilder builder = new StringBuilder();

    for (String key : keys) {
      if (builder.length() > 0) {
        builder.append(IKeyStroke.KEY_STROKE_SEPARATOR);
      }
      builder.append(key);
    }

    return builder.toString();
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public final void prepareAction() {
    try {
      prepareActionInternal();
      execPrepareAction();
    }
    catch (Throwable t) {
      LOG.warn("Action " + getClass().getName(), t);
    }
  }

  @Override
  public IActionUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * do not use this method, it is used internally by subclasses
   */
  protected void prepareActionInternal() throws ProcessingException {
  }

  @Override
  public ITypeWithClassId getContainer() {
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  @Override
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  protected class P_UIFacade implements IActionUIFacade {
    @Override
    public void fireActionFromUI() {
      try {
        if (isThisAndParentsEnabled() && isThisAndParentsVisible()) {
          doAction();
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      catch (Throwable e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected exception", e));
      }
    }

    @Override
    public void setSelectedFromUI(boolean b) {
      setSelected(b);
    }
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalActionExtension<OWNER extends AbstractAction> extends AbstractExtension<OWNER> implements IActionExtension<OWNER> {

    public LocalActionExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execSelectionChanged(ActionSelectionChangedChain chain, boolean selection) throws ProcessingException {
      getOwner().execSelectionChanged(selection);
    }

    @Override
    public void execAction(ActionActionChain chain) throws ProcessingException {
      getOwner().execAction();
    }

    @Override
    public void execInitAction(ActionInitActionChain chain) throws ProcessingException {
      getOwner().execInitAction();
    }

  }

  protected final void interceptSelectionChanged(boolean selection) throws ProcessingException {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionSelectionChangedChain chain = new ActionSelectionChangedChain(extensions);
    chain.execSelectionChanged(selection);
  }

  protected final void interceptAction() throws ProcessingException {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionActionChain chain = new ActionActionChain(extensions);
    chain.execAction();
  }

  protected final void interceptInitAction() throws ProcessingException {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionInitActionChain chain = new ActionInitActionChain(extensions);
    chain.execInitAction();
  }
}
