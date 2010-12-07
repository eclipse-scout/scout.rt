package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * see {@link AbstractDocumentField}
 */
public interface IDocumentField extends IValueField<File> {

  String PROP_RULERS_VISIBLE = "rulerVisible";
  String PROP_STATUS_BAR_VISIBLE = "statusBarVisible";

  void addDocumentFieldListener(DocumentFieldListener listener);

  void removeDocumentFieldListener(DocumentFieldListener listener);

  boolean isRulersVisible();

  void setRulersVisible(boolean b);

  boolean isStatusBarVisible();

  void setStatusBarVisible(boolean b);

  /**
   * insert text at current location
   */
  void insertText(String text);

  /**
   * save the document to the file specified
   * 
   * @param formatType
   *          doc, dot, odt, html, pdf, ...
   */
  void saveAs(File file, String formatType);

  void autoResizeDocument();

  void toggleRibbons();

  IDocumentFieldUIFacade getUIFacade();

}
