/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  DropType, Event, FileDropEvent, FormField, FormFieldLabelPosition, FormFieldStatusPosition, FormFieldStyle, FormFieldSuppressStatus, FormFieldTooltipAnchor, GridData, KeyStroke, Menu, PropertyChangeEvent, Status, WidgetEventMap
} from '../../index';

export interface FormFieldClipboardExportEvent<T = FormField> extends Event<T> {
  text: string;
}

export interface FormFieldEventMap extends WidgetEventMap {
  'clipboardExport': FormFieldClipboardExportEvent;
  'drop': Event<FormField> & FileDropEvent;
  'propertyChange:backgroundColor': PropertyChangeEvent<string>;
  'propertyChange:checkSaveNeeded': PropertyChangeEvent<boolean>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number>;
  'propertyChange:dropType': PropertyChangeEvent<DropType>;
  'propertyChange:empty': PropertyChangeEvent<boolean>;
  'propertyChange:errorStatus': PropertyChangeEvent<Status>;
  'propertyChange:fieldStyle': PropertyChangeEvent<FormFieldStyle>;
  'propertyChange:font': PropertyChangeEvent<string>;
  'propertyChange:foregroundColor': PropertyChangeEvent<string>;
  'propertyChange:gridData': PropertyChangeEvent<GridData>;
  'propertyChange:gridDataHints': PropertyChangeEvent<GridData>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[]>;
  'propertyChange:label': PropertyChangeEvent<string>;
  'propertyChange:labelBackgroundColor': PropertyChangeEvent<string>;
  'propertyChange:labelFont': PropertyChangeEvent<string>;
  'propertyChange:labelForegroundColor': PropertyChangeEvent<string>;
  'propertyChange:labelHtmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:labelPosition': PropertyChangeEvent<FormFieldLabelPosition>;
  'propertyChange:labelUseUiWidth': PropertyChangeEvent<boolean>;
  'propertyChange:labelVisible': PropertyChangeEvent<boolean>;
  'propertyChange:labelWidthInPixel': PropertyChangeEvent<number>;
  'propertyChange:mandatory': PropertyChangeEvent<boolean>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:menusVisible': PropertyChangeEvent<boolean>;
  'propertyChange:saveNeeded': PropertyChangeEvent<boolean>;
  'propertyChange:statusPosition': PropertyChangeEvent<FormFieldStatusPosition>;
  'propertyChange:statusVisible': PropertyChangeEvent<boolean>;
  'propertyChange:suppressStatus': PropertyChangeEvent<FormFieldSuppressStatus>;
  'propertyChange:tooltipAnchor': PropertyChangeEvent<FormFieldTooltipAnchor>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
  'propertyChange:touched': PropertyChangeEvent<boolean>;
}
