/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarComponent, CalendarComponentPopupEventMap, Label, WidgetModel, WidgetPopup} from "../index";
import CalendarComponentPopupModel, {CalendarComponentPopupWidgetMap} from './CalendarComponentPopupModel';
import $ from "jquery";

export class CalendarComponentPopup extends WidgetPopup<Label> {
  declare widgetMap: CalendarComponentPopupWidgetMap;
  declare eventMap: CalendarComponentPopupEventMap;
  declare parent: CalendarComponent;
  declare self: CalendarComponentPopup;

  protected override _jsonModel(): WidgetModel {
    return CalendarComponentPopupModel();
  }

  setLabel(label: string) {
    this.widget('ContentLabel').setValue(label);
    this._attachAppLinkHandler();
  }

  protected override _render() {
    super._render();
    this._attachAppLinkHandler();
  }

  protected _attachAppLinkHandler() {
    if (this.$container) {
      this.$container.find('.app-link').on('click', this._onAppLinkAction.bind(this));
    }
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  protected _onAppLinkAction(event: JQuery.ClickEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref') as string;
    this.triggerAppLinkAction(ref);
  }

  protected override _onMouseDownOutside(event: MouseEvent) {
    let $target = $(event.target);
    let yPosChanged = Math.abs(this.anchorBounds.y - event.y) > 10; // Close popup when mose is moved more than 10px
    if (!yPosChanged && this.parent._$parts.find($element => $element.isOrHas($target))) {
      // Do not close popup when mouse is not moved and clicked on the same component
      return;
    }
    super._onMouseDownOutside(event);
  }
}
