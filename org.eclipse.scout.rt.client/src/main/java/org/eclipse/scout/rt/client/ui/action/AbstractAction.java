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
package org.eclipse.scout.rt.client.ui.action;

import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionDisposeChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionInitActionChain;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionSelectionChangedChain;
import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStrokeNormalizer;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("d3cdbb0d-4c53-4854-b6f2-23465050c3c5")
public abstract class AbstractAction extends AbstractPropertyObserver implements IAction, IExtensibleObject {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractAction.class);
  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper();
  private static final NamedBitMaskHelper ENABLED_BIT_HELPER = new NamedBitMaskHelper();
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper();
  private static final String ACTION_RUNNING = "ACTION_RUNNING";
  private static final String INHERIT_ACCESSIBILITY = "INHERIT_ACCESSIBILITY";
  private static final String TOGGLE_ACTION = "TOGGLE_ACTION";
  private static final String SEPARATOR = "SEPARATOR";
  private static final String INITIALIZED = "INITIALIZED";
  private static final String ENABLED_INHERIT_ACCESSIBILITY = "ENABLED_INHERIT_ACCESSIBILITY";

  /**
   * Provides 8 dimensions for enabled state.<br>
   * Internally used: {@link IDimensions#ENABLED}, {@link IDimensions#ENABLED_GRANTED}, {@link #ACTION_RUNNING},
   * {@link #ENABLED_INHERIT_ACCESSIBILITY}.<br>
   * 4 dimensions remain for custom use. This Action is enabled, if all dimensions are enabled (all bits set).
   */
  private byte m_enabled;

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This Action is visible, if all dimensions are visible (all bits set).
   */
  private byte m_visible;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #INITIALIZED}, {@link #INHERIT_ACCESSIBILITY}, {@link #TOGGLE_ACTION}, {@link #SEPARATOR}
   */
  private byte m_flags;

  /**
   * {@link IAction#HORIZONTAL_ALIGNMENT_LEFT} or {@link IAction#HORIZONTAL_ALIGNMENT_RIGHT}
   */
  private byte m_horizontalAlignment;

  private final IActionUIFacade m_uiFacade;
  private final ObjectExtensions<AbstractAction, IActionExtension<? extends AbstractAction>> m_objectExtensions;

  public AbstractAction() {
    this(true);
  }

  public AbstractAction(boolean callInitializer) {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    m_enabled = NamedBitMaskHelper.ALL_BITS_SET; // default enabled
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    m_flags = NamedBitMaskHelper.NO_BITS_SET; // default all to false. are initialized in initConfig()
    m_objectExtensions = new ObjectExtensions<AbstractAction, IActionExtension<? extends AbstractAction>>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (isInitialized()) {
      return;
    }
    interceptInitConfig();
    setInitialized();
  }

  /**
   * This is the init of the runtime model after the environment (form, fields, ..) are built and configured
   */
  @Override
  public final void initAction() {
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
   * Defines the keystroke for this action. A keystroke is built from optional modifiers (alt, control, shift) and a key
   * (p, f11, delete). The keystroke has to follow a certain pattern: The modifiers (alt, shift, control) are separated
   * from the key by a '-'. Examples:
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
   * @return true if {@link #prepareAction()} should in addition consider the context of the action to decide for
   *         visibility and enabled.<br>
   *         For example a menu of a table field with {@link #isInheritAccessibility()}==true is invisible when the
   *         table field is disabled or invisible
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

  /**
   * @return true if the AbstractAction should be rendered as a separating line. If true, the AbstractAction can not
   *         trigger any action ({@link #execAction()})
   */
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
   * Configures the horizontal alignment of this action in the user interface. The horizontal alignment is only required
   * if the action is displayed in a menu-bar. Use {@link IAction#HORIZONTAL_ALIGNMENT_LEFT} or
   * {@link IAction#HORIZONTAL_ALIGNMENT_RIGHT}.
   * <p>
   * Subclasses can override this method. The default is {@link IAction#HORIZONTAL_ALIGNMENT_LEFT}.
   *
   * @return Horizontal alignment of this action.
   */
  @Order(130)
  @ConfigProperty(ConfigProperty.INTEGER)
  protected byte getConfiguredHorizontalAlignment() {
    return HORIZONTAL_ALIGNMENT_LEFT;
  }

  /**
   * Configures the css class(es) of this action.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(55)
  protected String getConfiguredCssClass() {
    return null;
  }

  /**
   * called by {@link #initAction()}<br>
   * this way a menu can for example add/remove custom child menus
   */
  @ConfigOperation
  @Order(10)
  protected void execInitAction() {
  }

  /**
   * called by {@link #dispose()}<br>
   */
  @ConfigOperation
  @Order(15)
  protected void execDispose() {
  }

  /**
   * called when action is performed independent of the selection state.
   */
  @ConfigOperation
  @Order(30)
  protected void execAction() {
  }

  /**
   * Called whenever the selection (of toggle-action) is changed.
   *
   * @param selection
   *          the new selection state
   */
  @ConfigOperation
  @Order(32)
  protected void execSelectionChanged(boolean selection) {
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
    setHorizontalAlignment(getConfiguredHorizontalAlignment());
    setCssClass(getConfiguredCssClass());
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
   * Calculates the actions's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
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
  public void doAction() {
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
   * Please double check if implementing this method! Consider using {@link #interceptAction()} instead. If no other
   * option ensure super call when overriding this method.
   */
  protected void doActionInternal() {
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
  public void setText(String text) {
    propertySupport.setPropertyString(PROP_TEXT, text);
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @Override
  public String getKeyStroke() {
    return propertySupport.getPropertyString(PROP_KEY_STROKE);
  }

  @Override
  public void setKeyStroke(String k) {
    KeyStrokeNormalizer scoutKeystroke = new KeyStrokeNormalizer(k);
    scoutKeystroke.normalize();
    if (scoutKeystroke.isValid()) {
      propertySupport.setPropertyString(PROP_KEY_STROKE, scoutKeystroke.getNormalizedKeystroke());
    }
    else {
      LOG.warn("Could not create keystroke '{}' because it is invalid!", k);
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

  private boolean isInitialized() {
    return FLAGS_BIT_HELPER.isBitSet(INITIALIZED, m_flags);
  }

  private void setInitialized() {
    m_flags = FLAGS_BIT_HELPER.setBit(INITIALIZED, m_flags);
  }

  @Override
  public boolean isSeparator() {
    return FLAGS_BIT_HELPER.isBitSet(SEPARATOR, m_flags);
  }

  @Override
  public void setSeparator(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(SEPARATOR, b, m_flags);
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
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  protected boolean setSelectedInternal(boolean b) {
    return propertySupport.setPropertyBool(PROP_SELECTED, b);
  }

  @Override
  public boolean isToggleAction() {
    return FLAGS_BIT_HELPER.isBitSet(TOGGLE_ACTION, m_flags);
  }

  @Override
  public void setToggleAction(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(TOGGLE_ACTION, b, m_flags);
  }

  @Override
  public boolean isInheritAccessibility() {
    return FLAGS_BIT_HELPER.isBitSet(INHERIT_ACCESSIBILITY, m_flags);
  }

  @Override
  public void setInheritAccessibility(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(INHERIT_ACCESSIBILITY, b, m_flags);
  }

  @Override
  public boolean isEnabledIncludingParents() {
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
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public void setEnabled(boolean enabled) {
    setEnabled(enabled, IDimensions.ENABLED);
  }

  @Override
  public void setEnabledInheritAccessibility(boolean b) {
    setEnabled(b, ENABLED_INHERIT_ACCESSIBILITY);
  }

  @Override
  public boolean isEnabledInheritAccessibility() {
    return isEnabled(ENABLED_INHERIT_ACCESSIBILITY);
  }

  /**
   * Access control<br>
   * when false, overrides isEnabled with false
   */
  @Override
  public void setEnabledPermission(Permission p) {
    boolean b;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setEnabledGranted(b);
  }

  @Override
  public boolean isEnabledGranted() {
    return isEnabled(IDimensions.ENABLED_GRANTED);
  }

  /**
   * Access control<br>
   * when false, overrides isEnabled with false
   */
  @Override
  public void setEnabledGranted(boolean enabled) {
    setEnabled(enabled, IDimensions.ENABLED_GRANTED);
  }

  @Override
  public boolean isEnabledProcessingAction() {
    return isEnabled(ACTION_RUNNING);
  }

  private void setEnabledProcessingAction(boolean enabledProcessing) {
    setEnabled(enabledProcessing, ACTION_RUNNING);
  }

  @Override
  public void setEnabled(boolean enabled, String dimension) {
    m_enabled = ENABLED_BIT_HELPER.changeBit(dimension, enabled, m_enabled);
    setEnabledInternal();
  }

  @Override
  public boolean isEnabled(String dimension) {
    return ENABLED_BIT_HELPER.isBitSet(dimension, m_enabled);
  }

  private void setEnabledInternal() {
    propertySupport.setPropertyBool(PROP_ENABLED, NamedBitMaskHelper.allBitsSet(m_enabled));
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE);
  }

  private void setVisibleInternal() {
    propertySupport.setPropertyBool(PROP_VISIBLE, NamedBitMaskHelper.allBitsSet(m_visible));
  }

  @Override
  public boolean isVisibleIncludingParents() {
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
  public void setVisible(boolean visible, String dimension) {
    m_visible = VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_visible);
    setVisibleInternal();
  }

  @Override
  public boolean isVisible(String dimension) {
    return VISIBLE_BIT_HELPER.isBitSet(dimension, m_visible);
  }

  @Override
  public void setVisiblePermission(Permission p) {
    boolean visibleByPerm = true;
    if (p != null) {
      visibleByPerm = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    setVisibleGranted(visibleByPerm);
  }

  @Override
  public boolean isVisibleGranted() {
    return isVisible(IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getContainer() instanceof ITypeWithClassId) {
      return simpleClassId + ID_CONCAT_SYMBOL + ((ITypeWithClassId) getContainer()).classId();
    }
    return simpleClassId;
  }

  /**
   * Combine a key stroke consisting of multiple keys.
   */
  public static String combineKeyStrokes(String... keys) {
    StringBuilder builder = new StringBuilder();

    for (String key : keys) {
      if (builder.length() > 0) {
        builder.append(IKeyStroke.KEY_STROKE_SEPARATOR);
      }
      builder.append(key);
    }

    return builder.toString();
  }

  @Override
  public IActionUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public IPropertyObserver getContainer() {
    return (IPropertyObserver) propertySupport.getProperty(PROP_CONTAINER);
  }

  @Override
  public void setContainerInternal(IPropertyObserver container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public byte getHorizontalAlignment() {
    return m_horizontalAlignment;
  }

  @Override
  public void setHorizontalAlignment(byte horizontalAlignment) {
    m_horizontalAlignment = horizontalAlignment;
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }

  @Override
  public void setView(boolean visible, boolean enabled) {
    setVisible(visible);
    setEnabled(enabled);
  }

  protected class P_UIFacade implements IActionUIFacade {
    @Override
    public void fireActionFromUI() {
      if (isEnabledIncludingParents() && isVisibleIncludingParents()) {
        doAction();
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
    public void execSelectionChanged(ActionSelectionChangedChain chain, boolean selection) {
      getOwner().execSelectionChanged(selection);
    }

    @Override
    public void execAction(ActionActionChain chain) {
      getOwner().execAction();
    }

    @Override
    public void execInitAction(ActionInitActionChain chain) {
      getOwner().execInitAction();
    }

    @Override
    public void execDispose(ActionDisposeChain chain) {
      getOwner().execDispose();
    }

  }

  protected final void interceptSelectionChanged(boolean selection) {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionSelectionChangedChain chain = new ActionSelectionChangedChain(extensions);
    chain.execSelectionChanged(selection);
  }

  protected final void interceptAction() {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionActionChain chain = new ActionActionChain(extensions);
    chain.execAction();
  }

  protected final void interceptInitAction() {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionInitActionChain chain = new ActionInitActionChain(extensions);
    chain.execInitAction();
  }

  protected final void interceptDispose() {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    ActionDisposeChain chain = new ActionDisposeChain(extensions);
    chain.execDispose();
  }

  @Override
  public final void dispose() {
    try {
      disposeInternal();
    }
    catch (RuntimeException e) {
      LOG.warn("Exception while disposing action.", e);
    }
    try {
      interceptDispose();
    }
    catch (RuntimeException e) {
      LOG.warn("Exception while disposing action.", e);
    }
  }

  protected void disposeInternal() {
  }

}
