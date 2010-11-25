/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.window.desktop.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

public class ScoutEditorMatchingStrategy implements IEditorMatchingStrategy {

  public boolean matches(IEditorReference editorRef, IEditorInput input) {
    if (!(editorRef.getEditor(false) instanceof AbstractScoutEditorPart)) {
      return false;
    }
    if (!(input instanceof ScoutFormEditorInput)) {
      return false;
    }
    final IEditorPart part = (IEditorPart) editorRef.getPart(false);
    if (part != null) {
      final IEditorInput editorInput = part.getEditorInput();
      return editorInput.equals(input);
    }
    return false;
  }

}
