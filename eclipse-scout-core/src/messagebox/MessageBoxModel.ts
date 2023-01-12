/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayParent, StatusSeverity, WidgetModel} from '../index';

export interface MessageBoxModel extends WidgetModel {
  header?: string;
  body?: string;
  severity?: StatusSeverity;
  iconId?: string;
  hiddenText?: string;
  html?: string;
  yesButtonText?: string;
  noButtonText?: string;
  cancelButtonText?: string;
  /**
   * Defines when the message box should be accessible (visible) and which part of the desktop is blocked for interaction.
   *
   * Possible parents are {@link Desktop}, {@link Outline} or {@link Form}:
   *
   * - Desktop: The message box is always accessible; blocks the entire desktop.
   * - Outline: The message box is only accessible when the given outline is active; blocks only the active outline, so changing the outline or using the desktop header in general is still possible.
   * - Form: The message box is only accessible when the given form is active; blocks only the form.
   *
   * By default, the {@link Desktop} is used as display parent.
   */
  displayParent?: DisplayParent;
}
