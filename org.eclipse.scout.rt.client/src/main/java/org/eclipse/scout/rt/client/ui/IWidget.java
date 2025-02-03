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

import java.security.Permission;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.visitor.IBreadthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.dimension.IEnabledDimension;

/**
 * Root interface for Scout UI components.
 *
 * @since 8.0
 */
public interface IWidget extends IPropertyObserver, IStyleable, IEnabledDimension, ITypeWithClassId {

  String PROP_INIT_CONFIG_DONE = "initConfigDone";
  String PROP_INIT_DONE = "initDone";
  String PROP_DISPOSE_DONE = "disposeDone";

  String PROP_LOADING = "loading";
  String PROP_ENABLED = "enabled";
  String PROP_PARENT_WIDGET = "parentWidget";
  String PROP_INHERIT_ACCESSIBILITY = "inheritAccessibility";

  /**
   * @return {@code true} if initConfig has been called, {@code false} if not.
   */
  boolean isInitConfigDone();

  /**
   * Init is supposed to be called after the widget has been created. This is usually done by the framework itself. E.g.
   * if the widgets are used on a form, the form will initialize all the widgets on form startup.
   * <p>
   * After the execution of this method, {@link #isInitDone()} will return true. A repeated execution of init() will do
   * nothing unless initDone would be set to false again which is done by {@link #reinit()} and {@link #dispose()}. This
   * means a widget may be initialized again after it has been disposed.
   * <p>
   * This method initializes this widget and all child widgets recursively.
   */
  void init();

  /**
   * @return {@code true} if {@link #init()} has been called, {@code false} if not. Also returns false if the widget has
   * been disposed.
   */
  boolean isInitDone();

  /**
   * Re initializes the widget all children by setting {@link #isInitDone()} to {@code false} and calling
   * {@link #init()} again.
   */
  void reinit();

  /**
   * Dispose needs to be called if the widget should not be used anymore in order to release any bound resources. This
   * is usually done by the framework itself. E.g. if the widgets are used on a form, the form will dispose all the
   * widgets when the form closes.
   * <p>
   * After the execution of this method, {@link #isDisposeDone()} will return true. A repeated execution of dispose()
   * will do nothing unless the disposeDone would be set to false which is done by {@link #init()}. This means a widget
   * may be initialized again after it has been disposed.
   * <p>
   * By calling this method the complete widget tree is disposed recursively.
   */
  void dispose();

  /**
   * @return {@code true} if {@link #dispose()} has been called, {@code false} if not. Also returns {@code false} if the
   * widget has been initialized again after it was disposed.
   */
  boolean isDisposeDone();

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Consumer} is never
   *     {@code null}.
   * @throws AssertionException
   *     if the visitor is {@code null}.
   */
  void visit(Consumer<IWidget> visitor);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once. The specified visitor is only
   * called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Consumer} is never
   *     {@code null}.
   * @param <T>
   *     The type of widget that should be visited.
   * @param type
   *     The type of widget that should be visited.
   * @throws AssertionException
   *     if one of the arguments is {@code null}.
   */
  <T extends IWidget> void visit(Consumer<T> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively according to the
   * {@link TreeVisitResult} returned by the specified {@link Function}.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Function} is never
   *     {@code null}. The {@link Function} returns a {@link TreeVisitResult} to control the traversal.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link Function}.
   * @throws AssertionException
   *     if the visitor is {@code null}.
   * @see TreeVisitResult
   */
  TreeVisitResult visit(Function<IWidget, TreeVisitResult> visitor);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively according to the
   * {@link TreeVisitResult} returned by the specified {@link Function}.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once. The specified visitor is only
   * called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Function} is never
   *     {@code null}. The {@link Function} returns a {@link TreeVisitResult} to control the traversal.
   * @param type
   *     The type of widget that should be visited.
   * @param <T>
   *     The type of widget that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link Function}.
   * @throws AssertionException
   *     if one of the arguments is {@code null}.
   * @see TreeVisitResult
   */
  <T extends IWidget> TreeVisitResult visit(Function<T, TreeVisitResult> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The specified {@link IDepthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IDepthFirstTreeVisitor#preVisit(Object, int, int)}. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *     The {@link IDepthFirstTreeVisitor} to call. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IDepthFirstTreeVisitor}.
   * @throws AssertionException
   *     if the visitor is {@code null}.
   * @see IDepthFirstTreeVisitor
   * @see TreeVisitResult
   */
  TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The specified {@link IDepthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IDepthFirstTreeVisitor#preVisit(Object, int, int)}. Every {@link IWidget} is only visited once.
   * The specified visitor is only called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *     The {@link IDepthFirstTreeVisitor} to call. Must not be {@code null}.
   * @param type
   *     The type of widget that should be visited.
   * @param <T>
   *     The type of widget that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IDepthFirstTreeVisitor}.
   * @throws AssertionException
   *     if one of the arguments is {@code null}.
   * @see IDepthFirstTreeVisitor
   * @see TreeVisitResult
   */
  <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively using a level-order strategy.
   * <p>
   * The specified {@link IBreadthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IBreadthFirstTreeVisitor#visit(Object, int, int)}. Every {@link IWidget} is only visited once.
   * The specified visitor is only called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *     The {@link IBreadthFirstTreeVisitor} to use. Must not be {@code null}.
   * @param type
   *     The type of widget that should be visited.
   * @param <T>
   *     The type of widget that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IBreadthFirstTreeVisitor}.
   * @throws AssertionException
   *     if one of the arguments is {@code null}.
   * @see IBreadthFirstTreeVisitor
   * @see TreeVisitResult
   */
  <T extends IWidget> TreeVisitResult visit(IBreadthFirstTreeVisitor<T> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively using a level-order strategy.
   * <p>
   * The specified {@link IBreadthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IBreadthFirstTreeVisitor#visit(Object, int, int)}. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *     The {@link IBreadthFirstTreeVisitor} to use. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IBreadthFirstTreeVisitor}.
   * @throws AssertionException
   *     if the visitor is {@code null}.
   * @see IBreadthFirstTreeVisitor
   * @see TreeVisitResult
   */
  TreeVisitResult visit(IBreadthFirstTreeVisitor<IWidget> visitor);

  /**
   * @return All child {@link IWidget}s. The resulting {@link List} is never {@code null} and does not contain any
   * {@code null} elements.
   */
  List<? extends IWidget> getChildren();

  Object getProperty(String name);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if necessary. Rather use the provided API instead.<br>
   *
   * @return true if property value changed
   */
  boolean setProperty(String name, Object value);

  boolean hasProperty(String name);

  /**
   * Tries to find a {@link IWidget widget} within this widget and all of its children recursively that has exactly the
   * given class (not {@code instanceof}).
   * <p>
   * The first widget in the child hierarchy that has exactly the given class will be returned. The hierarchy is
   * searched in a pre-order traversal.
   *
   * @param widgetClassToFind
   *     The class of the widget. Must not be {@code null}.
   * @return The first widget that is {@code instanceof} the given class or {@code null}.
   * @throws AssertionException
   *     if widgetClassToFind is {@code null}.
   */
  <T extends IWidget> T getWidgetByClass(Class<T> widgetClassToFind);

  /**
   * Flag to indicate whether this widget is currently loading data. Default is <code>false</code>. The exact
   * interpretation of this flag (and also if it should be respected at all) is left to the UI.
   */
  boolean isLoading();

  void setLoading(boolean loading);

  /**
   * @return {@code true} if this widget is enabled.
   * <p>
   * The result of this method does not respect any parent widgets. Use {@link #isEnabledIncludingParents()}
   * whenever necessary.
   */
  boolean isEnabled();

  /**
   * Sets the enabled state for the default dimension.
   * <p>
   * The actual enabled state depends on all dimensions. So the call to this method may not be sufficient to enable a
   * widget (if other dimensions are still disabled).
   *
   * @param enabled
   *     the new enabled state.
   */
  void setEnabled(boolean enabled);

  /**
   * @return {@code true} if this widget respects the enabled state of parent widgets. If property is set to
   * {@code false}, this widget may be enabled even if parent widgets are disabled.
   */
  boolean isInheritAccessibility();

  /**
   * @see #isInheritAccessibility()
   */
  void setInheritAccessibility(boolean inheritAccessibility);

  /**
   * Uses the {@link Permission} provided to calculated the enabled granted state of this widget.
   */
  void setEnabledPermission(Permission permission);

  /**
   * @return The value for the enabled granted dimension.
   */
  boolean isEnabledGranted();

  /**
   * @param enabledGranted
   *     the new value for the enabled granted dimension.
   */
  void setEnabledGranted(boolean enabledGranted);

  /**
   * Changes the default enabled dimension of this widget to the given value.
   *
   * @param enabled
   *     The new enabled value.
   * @param updateParents
   *     if <code>true</code> the enabled properties of all parent widgets are updated to same value as well. This
   *     argument only has an effect if the new enabled state is {@code true}.
   */
  void setEnabled(boolean enabled, boolean updateParents);

  /**
   * Changes the default enabled dimension of this widget to the given value.
   *
   * @param enabled
   *     The new enabled value.
   * @param updateParents
   *     if <code>true</code> the enabled properties of all parent widgets are updated to same value as well. This
   *     argument only has an effect if the new enabled state is {@code true}.
   * @param updateChildren
   *     if <code>true</code> the enabled properties of all child widgets (recursive) are updated to same value as
   *     well.
   */
  void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren);

  /**
   * Changes the granted enabled dimension of this widget to the given value.
   *
   * @param enabled
   *     The new enable-granted value.
   * @param updateParents
   *     if <code>true</code> the enabled properties of all parent widgets are updated to same value as well. This
   *     argument only has an effect if the new enabled state is {@code true}.
   */
  void setEnabledGranted(boolean enabled, boolean updateParents);

  /**
   * Changes the granted enabled dimension of this widget to the given value.
   *
   * @param enabled
   *     The new enable-granted value.
   * @param updateParents
   *     if <code>true</code> the enabled properties of all parent widgets are updated to same value as well. This
   *     argument only has an effect if the new enabled state is {@code true}.
   * @param updateChildren
   *     if <code>true</code> the enabled properties of all child widgets (recursive) are updated to same value as
   *     well.
   */
  void setEnabledGranted(boolean enabled, boolean updateParents, boolean updateChildren);

  /**
   * Checks all existing enabled dimensions of this widget if their enabled state equals the value returned by the
   * {@link Predicate} specified.
   *
   * @param filter
   *     A {@link Predicate} that is called for each enabled dimension. The corresponding enabled-bit of this
   *     widget must be equal to the result of the {@link Predicate}. In case {@code null} is passed all bits are
   *     compared against {@code true} (which is the same as {@link #isEnabled()}).
   * @return {@code true} if all enabled dimensions bits have the same value as returned by the specified
   * {@link Predicate}.
   */
  boolean isEnabled(Predicate<String> filter);

  /**
   * Changes the enabled-state value of the given dimension.
   *
   * @param enabled
   *     The new enabled-state value for the given dimension.
   * @param updateParents
   *     if <code>true</code> the enabled properties of all parent widgets are updated to same value as well. This
   *     argument only has an effect if the new enabled state is {@code true}.
   * @param dimension
   *     The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *     if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *     if too many dimensions are used. <b>Note:</b> these dimensions are shared amongst all items of an
   *     application. They are not available by instance but by class!
   */
  void setEnabled(boolean enabled, boolean updateParents, String dimension);

  /**
   * Changes the enabled-state value of the given dimension.
   *
   * @param enabled
   *     The new enabled-state value for the given dimension.
   * @param updateParents
   *     if <code>true</code> the enabled properties of all parent widgets are updated to same value as well. This
   *     argument only has an effect if the new enabled state is {@code true}.
   * @param updateChildren
   *     if <code>true</code> the enabled properties of all child widgets (recursive) are updated to same value as
   *     well.
   * @param dimension
   *     The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *     if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *     if too many dimensions are used. <b>Note:</b> these dimensions are shared amongst all elements of an
   *     application. They are not available by instance but by class!
   */
  void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren, String dimension);

  /**
   * @return the parent widget or {@code null} if this widget has no parent.
   * @see #getParentOfType(Class)
   * @see #visitParents(Predicate)
   */
  IWidget getParent();

  /**
   * @return The first parent widget that is {@code instanceof} the given type or {@code null} if no such parent can be
   * found.
   */
  <T extends IWidget> T getParentOfType(Class<T> type);

  /**
   * Sets the parent widget.
   * <p>
   * Do not use this internal method.
   */
  boolean setParentInternal(IWidget w);

  /**
   * @return {@code true} if this widget is enabled for all dimensions and all parent widgets are enabled for all
   * dimensions as well. The parent widgets are only considered as long as {@link #isInheritAccessibility()} is
   * {@code true}.
   */
  boolean isEnabledIncludingParents();

  /**
   * Visits all parent widgets. The receiver itself is not visited
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}.
   */
  boolean visitParents(Consumer<IWidget> visitor);

  /**
   * Visits all parent widgets that are {@code instanceof} the given type filter. The receiver itself is not visited.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}.
   */
  <T extends IWidget> boolean visitParents(Consumer<T> visitor, Class<T> typeFilter);

  /**
   * Visits all parent widgets as long as the {@link Predicate} specified returns {@code true}.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}. The visit aborts when the {@link Predicate} returns
   *     {@code false}.
   */
  boolean visitParents(Predicate<IWidget> visitor);

  /**
   * Visits all parent widgets that are {@code instanceof} the given type filter as long as the {@link Predicate}
   * specified returns {@code true}.
   *
   * @param visitor
   *     The visitor to call. Must not be {@code null}. The visit aborts when the {@link Predicate} returns
   *     {@code false}.
   */
  <T extends IWidget> boolean visitParents(Predicate<T> visitor, Class<T> typeFilter);

  /**
   * Checks whether the widget contains the given child. Uses the parent hierarchy which makes it faster than visiting
   * children. Hence, works only if the child widgets are properly connected to their parents ({@link #getParent()} must
   * not return null).
   *
   * @return true if the widget contains the given child, false if not.
   */
  boolean has(IWidget child);

  /**
   * @see #scrollToTop(ScrollOptions)
   */
  void scrollToTop();

  /**
   * Scrolls the widget to the top, if it is a scrollable container.
   */
  void scrollToTop(ScrollOptions options);

  /**
   * @see #reveal(ScrollOptions)
   */
  void reveal();

  /**
   * Brings the widget into view by scrolling the first scrollable parent. It has no effect on other display properties
   * (e.g. visible).
   */
  void reveal(ScrollOptions options);

  default void addWidgetListener(WidgetListener listener, Integer... eventTypes) {
    widgetListeners().add(listener, false, eventTypes);
  }

  default void removeWidgetListener(WidgetListener listener, Integer... eventTypes) {
    widgetListeners().remove(listener, eventTypes);
  }

  WidgetListeners widgetListeners();
}
