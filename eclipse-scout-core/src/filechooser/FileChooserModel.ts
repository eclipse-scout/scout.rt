/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DisplayParent, WidgetModel} from '../index';

export interface FileChooserModel extends WidgetModel {
  /**
   * Default is {@link FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE}
   */
  maximumUploadSize?: number;
  /**
   * @see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file#accept
   */
  acceptTypes?: string;
  /**
   * Default is false.
   */
  multiSelect?: boolean;
  /**
   * Defines when the file chooser should be accessible (visible) and which part of the desktop is blocked for interaction.
   *
   * Possible parents are {@link Desktop}, {@link Outline} or {@link Form}:
   *
   * - Desktop: The file chooser is always accessible; blocks the entire desktop.
   * - Outline: The file chooser is only accessible when the given outline is active; blocks only the active outline, so changing the outline or using the desktop header in general is still possible.
   * - Form: The file chooser is only accessible when the given form is active; blocks only the form.
   *
   * By default, the {@link Desktop} is used as display parent.
   */
  displayParent?: DisplayParent;
}
