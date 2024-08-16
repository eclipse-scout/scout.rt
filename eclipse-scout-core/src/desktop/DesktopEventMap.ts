/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BenchColumnLayoutData, Desktop, DesktopDisplayStyle, DesktopHistoryState, Event, FileChooser, Form, KeyStroke, Menu, MessageBox, NativeNotificationDefaults, Popup, PropertyChangeEvent, ViewButton, Widget, WidgetEventMap
} from '../index';

export interface DesktopCancelFormsEvent<T = Desktop> extends Event<T> {
  forms: Form[];
}

export interface DesktopFormActivateEvent<T = Desktop> extends Event<T> {
  form: Form;
}

export interface DesktopPopupOpenEvent<T = Desktop> extends Event<T> {
  popup: Popup;
}

export interface DesktopEventMap extends WidgetEventMap {
  'animationEnd': Event;
  'cancelForms': DesktopCancelFormsEvent;
  'dataChange': Event;
  'formActivate': DesktopFormActivateEvent;
  'historyEntryActivate': Event & DesktopHistoryState;
  'logoAction': Event;
  'outlineChange': Event;
  'popupOpen': DesktopPopupOpenEvent;
  'propertyChange:benchLayoutData': PropertyChangeEvent<BenchColumnLayoutData>;
  'propertyChange:benchVisible': PropertyChangeEvent<boolean>;
  'propertyChange:dense': PropertyChangeEvent<boolean>;
  'propertyChange:dialogs': PropertyChangeEvent<Form[]>;
  'propertyChange:displayStyle': PropertyChangeEvent<DesktopDisplayStyle>;
  'propertyChange:fileChoosers': PropertyChangeEvent<FileChooser[]>;
  'propertyChange:focusedElement': PropertyChangeEvent<Widget>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:inBackground': PropertyChangeEvent<boolean>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[]>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:messageBoxes': PropertyChangeEvent<MessageBox[]>;
  'propertyChange:nativeNotificationDefaults': PropertyChangeEvent<NativeNotificationDefaults>;
  'propertyChange:navigationHandleVisible': PropertyChangeEvent<boolean>;
  'propertyChange:navigationVisible': PropertyChangeEvent<boolean>;
  'propertyChange:splitterVisible': PropertyChangeEvent<boolean>;
  'propertyChange:theme': PropertyChangeEvent<string>;
  'propertyChange:viewButtons': PropertyChangeEvent<ViewButton[]>;
  'propertyChange:views': PropertyChangeEvent<Form[]>;
}
