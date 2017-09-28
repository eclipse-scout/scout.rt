package org.eclipse.scout.rt.client.ui.contenteditor;

import java.util.EventListener;

public interface ContentEditorFieldListener extends EventListener {

  void contentEditorFieldChanged(ContentEditorFieldEvent e);
}
