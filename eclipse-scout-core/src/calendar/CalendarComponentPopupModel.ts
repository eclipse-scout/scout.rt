/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Label, Popup, WidgetPopup, WidgetPopupModel} from '../index';

export default (): WidgetPopupModel<Label> => ({
  objectType: WidgetPopup<Label>,
  id: 'CalendarComponentPopup',
  closeOnAnchorMouseDown: true,
  closeOnMouseDownOutside: true,
  closeOnOtherPopupOpen: true,
  horizontalAlignment: Popup.Alignment.LEFT,
  verticalAlignment: Popup.Alignment.CENTER,
  trimWidth: false,
  trimHeight: true,
  horizontalSwitch: true,
  verticalSwitch: false,
  withArrow: true,
  cssClass: 'popup',
  scrollType: 'remove',
  content: {
    objectType: Label,
    id: 'ContentLabel',
    htmlEnabled: true,
    scrollable: true,
    cssClass: 'calendar-component-tooltip-content tooltip-content'
  }
});

/* **************************************************************************
* GENERATED WIDGET MAPS
* **************************************************************************/

export type CalendarComponentPopupWidgetMap = {
  'ContentLabel': Label;
};
