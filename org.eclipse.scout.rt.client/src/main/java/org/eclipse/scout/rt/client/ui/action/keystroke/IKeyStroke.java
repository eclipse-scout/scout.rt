/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.keystroke;

import org.eclipse.scout.rt.client.ui.action.IAction;

/**
 * A key stroke consists of a action and a key spec<br>
 * The key spec is a key name together with the modifiers shift, control, alt
 *
 * @see IAction#getKeyStroke()
 */
public interface IKeyStroke extends IAction {

  String SHIFT = "shift";
  String CONTROL = "control";
  String ALT = "alt";

  String DELETE = "delete";
  String INSERT = "insert";
  String ENTER = "enter";
  String ESCAPE = "escape";
  String SPACE = "space";
  String TAB = "tab";
  String BACKSPACE = "backspace";

  String LEFT = "left";
  String RIGHT = "right";
  String UP = "up";
  String DOWN = "down";

  String F1 = "f1";
  String F2 = "f2";
  String F3 = "f3";
  String F4 = "f4";
  String F5 = "f5";
  String F6 = "f6";
  String F7 = "f7";
  String F8 = "f8";
  String F9 = "f9";
  String F10 = "f10";
  String F11 = "f11";
  String F12 = "f12";

  String COMMA = "comma";
  String POINT = "point";
}
