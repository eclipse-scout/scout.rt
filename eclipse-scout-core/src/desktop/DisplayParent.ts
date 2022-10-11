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
import {FileChooser, FileChooserController, Form, FormController, MessageBox, MessageBoxController, Widget} from '../index';

export default interface DisplayParent extends Widget {

  views: Form[];
  dialogs: Form[];
  formController: FormController;

  messageBoxes: MessageBox[];
  messageBoxController: MessageBoxController;

  fileChoosers: FileChooser[];
  fileChooserController: FileChooserController;

  inFront(): boolean;

  acceptView?(view: Widget): boolean;

  acceptDialog?(dialog: Widget): boolean;

  onGlassPaneMouseDown?(glassPaneOwner: Widget, $glassPane: JQuery<HTMLDivElement>);
}
