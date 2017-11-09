package org.eclipse.scout.rt.client.ui.contenteditor;

/**
 * @since 7.1
 */
public interface IContentEditorFieldUIFacade {

  void editElementFromUI(ContentElement contentElement);

  void setContentFromUI(String content);
}
