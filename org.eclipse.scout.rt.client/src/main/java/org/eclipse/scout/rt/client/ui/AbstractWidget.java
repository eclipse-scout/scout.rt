/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.security.Permission;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.IBreadthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeTraversals;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.security.ACCESS;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 8.0
 */
@ClassId("c11a79f7-0af6-430e-9700-2d050e3aa41e")
public abstract class AbstractWidget extends AbstractPropertyObserver implements IWidget {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractWidget.class);
  private static final NamedBitMaskHelper ENABLED_BIT_HELPER = new NamedBitMaskHelper(IDimensions.ENABLED, IDimensions.ENABLED_GRANTED, IDimensions.ENABLED_SLAVE);
  private static final String PROP_ENABLED_BYTE = "enabledByte";

  private final WidgetListeners m_listenerList;

  public AbstractWidget() {
    this(true);
  }

  public AbstractWidget(boolean callInitializer) {
    m_listenerList = new WidgetListeners();
    setEnabledByte(NamedBitMaskHelper.ALL_BITS_SET, false); // default enabled
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    if (isInitConfigDone()) {
      return;
    }
    initConfigInternal();
    setInitConfigDone(true);
  }

  /**
   * Will be called by {@link #callInitializer()} but only if {@link #isInitConfigDone()} returns false.
   */
  protected void initConfigInternal() {
    initConfig();
  }

  protected void initConfig() {
    setCssClass(getConfiguredCssClass());
    setEnabled(getConfiguredEnabled());
    setInheritAccessibility(getConfiguredInheritAccessibility());
  }

  @Override
  public boolean isInitConfigDone() {
    return propertySupport.getPropertyBool(PROP_INIT_CONFIG_DONE);
  }

  protected void setInitConfigDone(boolean initConfigDone) {
    propertySupport.setPropertyBool(PROP_INIT_CONFIG_DONE, initConfigDone);
  }

  @Override
  public final void init() {
    if (isInitDone()) {
      return;
    }
    doInit(true);
  }

  @Override
  public <T extends IWidget> T getWidgetByClass(Class<T> widgetClassToFind) {
    assertNotNull(widgetClassToFind);

    final Holder<T> result = new Holder<>(widgetClassToFind);
    Function<IWidget, TreeVisitResult> visitor = w -> {
      if (w instanceof AbstractWidget) {
        return ((AbstractWidget) w).getWidgetByClassInternal(result, widgetClassToFind);
      }
      return TreeVisitResult.CONTINUE;
    };
    visit(visitor);
    return result.getValue();
  }

  public <T extends IWidget> TreeVisitResult getWidgetByClassInternal(Holder<T> result, Class<T> widgetClassToFind) {
    if (widgetClassToFind.isInstance(this)) {
      result.setValue(widgetClassToFind.cast(this));
      return TreeVisitResult.TERMINATE;
    }
    return TreeVisitResult.CONTINUE;
  }

  private void doInit(boolean recursive) {
    initInternal();
    if (recursive) {
      initChildren();
    }

    setInitDone(true);
    setDisposeDone(false);
  }

  protected void initChildren() {
    initChildren(getChildren());
  }

  protected void initChildren(List<? extends IWidget> widgets) {
    for (IWidget w : widgets) {
      w.init();
    }
  }

  /**
   * Will be called by {@link #init()} but only if {@link #isInitDone()} returns false.<br>
   * This method should initialize this instance and non-widget-children only. Child widgets will be initialized in
   * {@link #initChildren()}.
   */
  protected void initInternal() {
    // nop
  }

  @Override
  public boolean isInitDone() {
    return propertySupport.getPropertyBool(PROP_INIT_DONE);
  }

  protected void setInitDone(boolean initDone) {
    propertySupport.setPropertyBool(PROP_INIT_DONE, initDone);
  }

  @Override
  public void reinit() {
    setInitDone(false);
    doInit(false);
    for (IWidget w : getChildren()) {
      w.reinit();
    }
  }

  @Override
  public final void dispose() {
    if (isDisposeDone()) {
      return;
    }

    disposeChildren();

    try {
      disposeInternal();
    }
    catch (RuntimeException t) {
      LOG.warn("Could not dispose widget '{}'.", this, t);
    }

    setDisposeDone(true);
    setInitDone(false);
  }

  protected void disposeChildren() {
    disposeChildren(getChildren());
  }

  protected void disposeChildren(List<? extends IWidget> widgetsToDispose) {
    for (IWidget w : widgetsToDispose) {
      try {
        w.dispose();
      }
      catch (RuntimeException t) {
        LOG.warn("Could not dispose widget '{}'.", w, t);
      }
    }
  }

  /**
   * Will be called by {@link #dispose()} but only if {@link #isDisposeDone()} returns false.<br>
   * This method should only dispose this single instance and non-widget children. Widget children are disposed
   * automatically in {@link #disposeChildren()}.
   */
  protected void disposeInternal() {
    // nop
  }

  @Override
  public boolean isDisposeDone() {
    return propertySupport.getPropertyBool(PROP_DISPOSE_DONE);
  }

  protected void setDisposeDone(boolean disposeDone) {
    propertySupport.setPropertyBool(PROP_DISPOSE_DONE, disposeDone);
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(Function<T, TreeVisitResult> visitor, Class<T> type) {
    return visit(new WidgetVisitorTypeAdapter<>(visitor, type));
  }

  @Override
  public <T extends IWidget> void visit(Consumer<T> visitor, Class<T> type) {
    assertNotNull(visitor);
    visit(new WidgetVisitorTypeAdapter<>(widget -> {
      visitor.accept(widget);
      return TreeVisitResult.CONTINUE;
    }, type));
  }

  @Override
  public void visit(Consumer<IWidget> visitor) {
    assertNotNull(visitor);
    visit(widget -> {
      visitor.accept(widget);
      return TreeVisitResult.CONTINUE;
    });
  }

  @Override
  public TreeVisitResult visit(Function<IWidget, TreeVisitResult> visitor) {
    return visit(WidgetVisitorTypeAdapter.functionToVisitor(visitor));
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Class<T> type) {
    return visit(new WidgetVisitorTypeAdapter<>(visitor, type));
  }

  @Override
  public TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor) {
    return visit(visitor, IWidget::getChildren);
  }

  @SuppressWarnings("unchecked")
  protected <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Function<T, Collection<? extends IWidget>> childrenSupplier, Class<T> type) {
    IDepthFirstTreeVisitor<IWidget> widgetVisitorTypeAdapter = new WidgetVisitorTypeAdapter<>(visitor, type);
    Function<IWidget, Collection<? extends IWidget>> cs = widget -> {
      if (type.isAssignableFrom(widget.getClass())) {
        childrenSupplier.apply((T) widget);
      }
      return widget.getChildren();
    };
    return visit(widgetVisitorTypeAdapter, cs);
  }

  protected TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor, Function<IWidget, Collection<? extends IWidget>> childrenSupplier) {
    return TreeTraversals.create(visitor, childrenSupplier).traverse(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IWidget> TreeVisitResult visit(IBreadthFirstTreeVisitor<T> visitor, Class<T> type) {
    return visit((widget, level, index) -> {
      if (type.isAssignableFrom(widget.getClass())) {
        return visitor.visit((T) widget, level, index);
      }
      return TreeVisitResult.CONTINUE;
    });
  }

  @Override
  public TreeVisitResult visit(IBreadthFirstTreeVisitor<IWidget> visitor) {
    return TreeTraversals.create(visitor, IWidget::getChildren).traverse(this);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.emptyArrayList(); // by default a widget has no children
  }

  /**
   * Configures whether this widget is enabled or not.
   * <p>
   * The value returned by this method is used for the default enabled dimension only.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this widget is enabled and {@code false} otherwise.
   * @see #setEnabled(boolean, String)
   * @see #isEnabledIncludingParents()
   */
  @Order(10)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  /**
   * Configures whether this widgets inherits the enabled state of its parent widgets.
   * <p>
   * For example a menu of a table field with {@link #isInheritAccessibility()}{@code == true} is automatically disabled
   * if the table field is disabled.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this widget is only enabled if all parent widgets are enabled as well. {@code false} if the
   * enabled state of this widget is independent of the parent widgets.
   * @see #setInheritAccessibility(boolean)
   * @see #isEnabledIncludingParents()
   */
  @Order(20)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredInheritAccessibility() {
    return true;
  }

  /**
   * Configures the css class(es) of this widget.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @Order(30)
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredCssClass() {
    return null;
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
  public Object getProperty(String name) {
    return propertySupport.getProperty(name);
  }

  @Override
  public boolean setProperty(String name, Object value) {
    return propertySupport.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public boolean isLoading() {
    return propertySupport.getPropertyBool(PROP_LOADING);
  }

  @Override
  public void setLoading(boolean loading) {
    propertySupport.setPropertyBool(PROP_LOADING, loading);
  }

  @Override
  public boolean isEnabled() {
    return NamedBitMaskHelper.allBitsSet(getEnabledByte());
  }

  @Override
  public void setEnabled(boolean enabled) {
    setEnabled(enabled, IDimensions.ENABLED);
  }

  @Override
  public boolean isEnabledGranted() {
    return isEnabled(IDimensions.ENABLED_GRANTED);
  }

  @Override
  public void setEnabledGranted(boolean enabled) {
    setEnabled(enabled, IDimensions.ENABLED_GRANTED);
  }

  @Override
  public boolean isEnabled(String dimension) {
    return ENABLED_BIT_HELPER.isBitSet(dimension, getEnabledByte());
  }

  @Override
  public boolean isInheritAccessibility() {
    return propertySupport.getPropertyBool(PROP_INHERIT_ACCESSIBILITY);
  }

  @Override
  public void setInheritAccessibility(boolean b) {
    propertySupport.setPropertyBool(PROP_INHERIT_ACCESSIBILITY, b);
  }

  @Override
  public void setEnabledPermission(Permission permission) {
    boolean b = true;
    if (permission != null) {
      b = ACCESS.check(permission);
    }
    setEnabledGranted(b);
  }

  @Override
  public void setEnabledGranted(boolean enabled, boolean updateParents) {
    setEnabledGranted(enabled, updateParents, false);
  }

  @Override
  public void setEnabledGranted(boolean enabled, boolean updateParents, boolean updateChildren) {
    setEnabled(enabled, updateParents, updateChildren, IDimensions.ENABLED_GRANTED);
  }

  @Override
  public void setEnabled(final boolean enabled, final boolean updateParents) {
    setEnabled(enabled, updateParents, false);
  }

  @Override
  public void setEnabled(final boolean enabled, final boolean updateParents, final boolean updateChildren) {
    setEnabled(enabled, updateParents, updateChildren, IDimensions.ENABLED);
  }

  @Override
  public void setEnabled(boolean enabled, String dimension) {
    setEnabled(enabled, false, dimension);
  }

  @Override
  public void setEnabled(final boolean enabled, final boolean updateParents, final String dimension) {
    setEnabled(enabled, updateParents, false, dimension);
  }

  @Override
  public void setEnabled(final boolean enabled, final boolean updateParents, final boolean updateChildren, final String dimension) {
    setEnabledByte(ENABLED_BIT_HELPER.changeBit(dimension, enabled, getEnabledByte()), true);

    if (enabled && updateParents) {
      // also enable all parents
      visitParents(field -> {
        field.setEnabled(true, dimension);
      });
    }

    if (updateChildren) {
      // propagate change to children
      for (IWidget w : getChildren()) {

        w.visit(field -> {
          if (field instanceof IAction) {
            if (field.isInheritAccessibility()) {
              field.setEnabled(enabled, dimension);
            }
          }
          else {
            field.setEnabled(enabled, dimension);
          }
        });
      }
    }
  }

  @Override
  public boolean isEnabled(Predicate<String> filter) {
    return ENABLED_BIT_HELPER.allBitsEqual(getEnabledByte(), filter);
  }

  private byte getEnabledByte() {
    return propertySupport.getPropertyByte(PROP_ENABLED_BYTE);
  }

  private boolean setEnabledByte(byte enabled, boolean fireListeners) {
    boolean changed = propertySupport.setPropertyByte(PROP_ENABLED_BYTE, enabled);
    if (changed && fireListeners) {
      boolean newEnabled = isEnabled();
      propertySupport.firePropertyChange(PROP_ENABLED, !newEnabled, newEnabled);
    }
    return changed;
  }

  @Override
  public IWidget getParent() {
    return (IWidget) propertySupport.getProperty(PROP_PARENT_WIDGET);
  }

  @Override
  public boolean setParentInternal(IWidget w) {
    return propertySupport.setProperty(PROP_PARENT_WIDGET, w);
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean isEnabledIncludingParents() {
    if (!isEnabled()) {
      return false;
    }
    if (!isInheritAccessibility()) {
      return true;
    }

    AtomicReference<Boolean> result = new AtomicReference<>(true);
    visitParents(w -> {
      if (!w.isEnabled()) {
        result.set(false);
        return false; // disabled parent found: cancel search
      }
      if (!w.isInheritAccessibility()) {
        return false; // no need to check any more parents
      }
      return true; // check next parent
    });
    return result.get();
  }

  @Override
  public boolean visitParents(Consumer<IWidget> visitor) {
    return visitParents(visitor, IWidget.class);
  }

  @Override
  public <T extends IWidget> boolean visitParents(Consumer<T> visitor, Class<T> typeFilter) {
    return visitParents(w -> {
      visitor.accept(w);
      return true;
    }, typeFilter);
  }

  @Override
  public boolean visitParents(Predicate<IWidget> visitor) {
    return visitParents(visitor, IWidget.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IWidget> boolean visitParents(Predicate<T> visitor, Class<T> typeFilter) {
    IWidget cur = this;
    while ((cur = cur.getParent()) != null) {
      if (typeFilter.isInstance(cur) && !visitor.test((T) cur)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public <T extends IWidget> T getParentOfType(Class<T> type) {
    AtomicReference<T> result = new AtomicReference<>();
    visitParents(composite -> !result.compareAndSet(null, composite), type);
    return result.get();
  }

  @Override
  public boolean has(IWidget child) {
    while (child != null) {
      if (this.equals(child.getParent())) {
        return true;
      }
      child = child.getParent();
    }
    return false;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    IWidget parent = getParent();
    if (parent != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + parent.classId();
    }
    return simpleClassId;
  }

  @Override
  public void scrollToTop() {
    scrollToTop(null);
  }

  @Override
  public void scrollToTop(ScrollOptions options) {
    WidgetEvent event = new WidgetEvent(this, WidgetEvent.TYPE_SCROLL_TO_TOP);
    event.setScrollOptions(options);
    fireWidgetEvent(event);
  }

  @Override
  public void reveal() {
    reveal(null);
  }

  @Override
  public void reveal(ScrollOptions options) {
    WidgetEvent event = new WidgetEvent(this, WidgetEvent.TYPE_REVEAL);
    event.setScrollOptions(options);
    fireWidgetEvent(event);
  }

  public final void notifyFocusIn() {
    fireWidgetEvent(new WidgetEvent(this, WidgetEvent.TYPE_FOCUS_IN));
    execFocusIn();
  }

  public final void notifyFocusOut() {
    fireWidgetEvent(new WidgetEvent(this, WidgetEvent.TYPE_FOCUS_OUT));
    execFocusOut();
  }

  /**
   * Function is called when the widget gains the focus, but only if {@link IDesktop#isTrackFocus()} is set to true.
   */
  protected void execFocusIn() {
    // nop
  }

  /**
   * Function is called when the widget looses the focus, but only if {@link IDesktop#isTrackFocus()} is set to true.
   */
  protected void execFocusOut() {
    // nop
  }

  protected void fireWidgetEvent(WidgetEvent event) {
    widgetListeners().fireEvent(event);
  }

  @Override
  public WidgetListeners widgetListeners() {
    return m_listenerList;
  }
}
