/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisabledStyle, LogicalGrid, ObjectType, ObjectWithUuidModel, Session, Widget} from '../index';

export interface WidgetModel extends ObjectWithUuidModel<Widget> {
  /**
   * The creation of a widget requires a parent so that a link between the child and the parent widget (and eventually between all widgets on the desktop) can be established.
   *
   * The parent is typically the same as the {@link owner}, but there may be cases when they (temporarily) diverge:
   * If a widget should temporarily be used by another widget (like a popup), the parent will be changed to that other widget (the popup) but the owner stays the same.
   * This means the popup is now the temporary parent. If the popup is destroyed its children won't be because the popup is not the owner.
   *
   * *Example*: {@link ViewMenuPopup} uses the {@link ViewButton}s as menu items. These view buttons are owned by the desktop and must therefore not be destroyed
   * when the popup closes, otherwise they could not be reused the second time the popup opens.
   *
   * @see https://eclipsescout.github.io/stable/technical-guide-js.html#parent-and-owner
   */
  parent?: Widget;
  /**
   * When a widget is destroyed, all children that have the widget as owner will be destroyed as well.
   * This means, only the owner is allowed to destroy its children when it is being destroyed.
   *
   * By default, the owner is derived from the {@link parent}, so it does not need to be set explicitly.
   *
   * @see https://eclipsescout.github.io/stable/technical-guide-js.html#parent-and-owner
   */
  owner?: Widget;
  /**
   * Links the {@link Session} to the widget, so it can be easily accessed e.g. for retrieving texts (`this.session.text('MyText')`).
   *
   * By default, the session of the {@link parent} is used, so it does not need to be set explicitly.
   */
  session?: Session;
  /**
   * Defines whether the widget should be enabled.
   *
   * If it is disabled, the user can neither modify the widget itself nor its children, unless {@link inheritAccessibility} is set to false on a child.
   *
   * Enabled is a multidimensional property, which means, the widget will only be enabled if every dimension is true.
   *
   * If a boolean is passed, the value will be used for the 'default' dimension.
   * Alternatively, an object can be passed containing the dimensions. If a dimension is not set explicitly, it defaults to true.
   *
   * The available dimensions are:
   * - default: The default dimension.
   * - granted: Defines whether the widget is allowed to be enabled, can also be set by {@link enabledGranted}.
   *
   * *Note*: to check whether a widget is accessible, always use {@link Widget.enabledComputed}. This property considers the {@link enabled} state of the ancestors as well.
   * So, whenever the {@link enabled} property of a widget changes, the {@link enabledComputed} property of the widget and its children will be updated (see {@link recomputeEnabled}).
   *
   * Default is true.
   */
  enabled?: boolean | Record<string, boolean>;
  /**
   * Defines whether the widget is allowed to be enabled.
   *
   * This property sets the 'granted' dimension for the {@link enabled} property and therefore influences the computed state.
   *
   * Default is true.
   */
  enabledGranted?: boolean;
  /**
   * If enabled, the widget keeps track of the active element inside the container,
   * so that it can restore the focus using {@link Widget.restoreFocus} whenever the widget is rendered or attached again.
   *
   * Default is false.
   */
  trackFocus?: boolean;
  /**
   * Configures the number of pixels that the {@link Widget.get$Scrollable} is scrolled vertically.
   *
   * The property is automatically updated whenever the content is scrolled so the scroll position can be reverted when the widget is rendered again after it has been removed.
   */
  scrollTop?: number;
  /**
   * Configures the number of pixels that the {@link Widget.get$Scrollable} is scrolled horizontally.
   *
   * The property is automatically updated whenever the content is scrolled so the scroll position can be reverted when the widget is rendered again after it has been removed.
   */
  scrollLeft?: number;
  /**
   * Controls whether the widget should inherit the enabled state from its ancestors.
   *
   * - If set to true, the widget is disabled if any ancestor widget is disabled.
   * - If set to false, the widget is not affected by the enabled state of its ancestor and may be enabled even if an ancestor is disabled.
   *
   * Default is true.
   */
  inheritAccessibility?: boolean;
  /**
   * Configures the look of a disabled widget.
   *
   * Default is {@link DisabledStyle.DEFAULT}.
   */
  disabledStyle?: DisabledStyle;
  /**
   * Defines whether the widget should be visible.
   *
   * Visible is a multidimensional property, which means, the widget will only be visible if every dimension is true.
   *
   * If a boolean is passed, the value will be used for the 'default' dimension.
   * Alternatively, an object can be passed containing the dimensions. If a dimension is not set explicitly, it defaults to true.
   *
   * The available dimensions are:
   * - default: The default dimension.
   * - granted: Defines whether the widget is allowed to be visible, can also be set by {@link visibleGranted}.
   *
   * Default is true.
   */
  visible?: boolean | Record<string, boolean>;
  /**
   * Defines whether the widget is allowed to be visible.
   *
   * This property sets the 'granted' dimension for the {@link visible} property and therefore influences the computed state.
   *
   * Default is true.
   */
  visibleGranted?: boolean;
  /**
   * Configures the custom css classes on the widget.
   *
   * The property accepts one or more css classes separated by space.
   *
   * By default, there are no custom css classes.
   */
  cssClass?: string;
  /**
   * Configures whether the widget is in loading state. It depends on the concrete widget whether this state is visualized.
   *
   * Default is false.
   */
  loading?: boolean;
  /**
   * Widgets using {@link LogicalGridLayout} may have a grid to calculate the {@link GridData} of the children.
   *
   * In that case, the {@link LogicalGridLayout} will validate the {@link LogicalGrid} using {@link LogicalGrid.validate} before the layout
   * to ensure all widgets in the grid have an up to date {@link GridData} object.
   */
  logicalGrid?: LogicalGrid | ObjectType<LogicalGrid>;
  /**
   * Configures whether the {@link Widget.remove} operation should be animated.
   *
   * If set to true, {@link Widget.remove} won't remove the element immediately but after the animation has been finished.
   * This expects a css animation which may be triggered by the class `animate-remove`.
   *
   * If the browser does not support css animations, remove will be executed immediately.
   */
  animateRemoval?: boolean;
  /**
   * The class to be added onto the widget to trigger the remove animation.
   *
   * @see animateRemoval
   *
   * Default is `animate-remove`.
   */
  animateRemovalClass?: string;

  [property: string]: any; // allow custom properties
}
