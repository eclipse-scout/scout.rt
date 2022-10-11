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
import {BenchColumnLayoutData, Desktop, Event, Form, KeyStroke, Menu, Popup, PropertyChangeEvent, ViewButton, Widget, WidgetEventMap} from '../index';
import {DesktopDisplayStyle, DesktopHistoryState, NativeNotificationDefaults} from './Desktop';

export interface DesktopCancelFormsEvent<T = Desktop> extends Event<T> {
  forms: Form[];
}

export interface DesktopFormActivateEvent<T = Desktop> extends Event<T> {
  form: Form;
}

export interface DesktopPopupOpenEvent<T = Desktop> extends Event<T> {
  popup: Popup;
}

export default interface DesktopEventMap extends WidgetEventMap {
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
  'propertyChange:displayStyle': PropertyChangeEvent<DesktopDisplayStyle>;
  'propertyChange:focusedElement': PropertyChangeEvent<Widget>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:inBackground': PropertyChangeEvent<boolean>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[]>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:nativeNotificationDefaults': PropertyChangeEvent<NativeNotificationDefaults>;
  'propertyChange:navigationHandleVisible': PropertyChangeEvent<boolean>;
  'propertyChange:navigationVisible': PropertyChangeEvent<boolean>;
  'propertyChange:splitterVisible': PropertyChangeEvent<boolean>;
  'propertyChange:theme': PropertyChangeEvent<string>;
  'propertyChange:viewButtons': PropertyChangeEvent<ViewButton[]>;
  'propertyChange:views': PropertyChangeEvent<Form[]>;
}
