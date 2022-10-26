/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, FormField, GridData, KeyStroke, Menu, PropertyChangeEvent, Status, WidgetEventMap} from '../../index';
import {DropType, FileDropEvent} from '../../util/dragAndDrop';
import {FormFieldLabelPosition, FormFieldStatusPosition, FormFieldStyle, FormFieldSuppressStatus, FormFieldTooltipAnchor} from './FormField';

export interface FormFieldClipboardExportEvent<T = FormField> extends Event<T> {
  text: string;
}

export default interface FormFieldEventMap extends WidgetEventMap {
  'clipboardExport': FormFieldClipboardExportEvent;
  'drop': Event<FormField> & FileDropEvent;
  'propertyChange:backgroundColor': PropertyChangeEvent<string>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number>;
  'propertyChange:dropType': PropertyChangeEvent<DropType>;
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
  'propertyChange:statusPosition': PropertyChangeEvent<FormFieldStatusPosition>;
  'propertyChange:statusVisible': PropertyChangeEvent<boolean>;
  'propertyChange:suppressStatus': PropertyChangeEvent<FormFieldSuppressStatus>;
  'propertyChange:tooltipAnchor': PropertyChangeEvent<FormFieldTooltipAnchor>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
  'propertyChange:touched': PropertyChangeEvent<boolean>;
}
