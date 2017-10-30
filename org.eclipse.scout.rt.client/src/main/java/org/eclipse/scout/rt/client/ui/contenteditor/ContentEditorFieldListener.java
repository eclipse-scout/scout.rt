package org.eclipse.scout.rt.client.ui.contenteditor;

import java.util.EventListener;

/**
 * @since 7.1
 */
public interface ContentEditorFieldListener extends EventListener {

  void contentEditorFieldChanged(ContentEditorFieldEvent e);
}
