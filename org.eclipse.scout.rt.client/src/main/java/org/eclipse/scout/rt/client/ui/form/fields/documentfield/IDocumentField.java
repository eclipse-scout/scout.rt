package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * see {@link AbstractDocumentField}
 */
public interface IDocumentField extends IValueField<RemoteFile> {

  String PROP_RULERS_VISIBLE = "rulerVisible";
  String PROP_STATUS_BAR_VISIBLE = "statusBarVisible";
  String PROP_COM_READY = "comReady";

  void addDocumentFieldListener(DocumentFieldListener listener);

  void removeDocumentFieldListener(DocumentFieldListener listener);

  boolean isRulersVisible();

  void setRulersVisible(boolean b);

  boolean isStatusBarVisible();

  void setStatusBarVisible(boolean b);

  boolean isComReady();

  /**
   * <p>
   * Saves the document without updating the value of this document field.
   * </p>
   * <p>
   * When the save of the document (for example in format type html) produces multiple files, then the created
   * RemoteFile contains compressed data (*.zip).
   * </p>
   * <p>
   * If the format is html, a plain-text representation of the document is created as well. Thereby, the name of this
   * file corresponds to the given name and txt as file extension. This file is located in the root directory of the
   * archive generated.
   * </p>
   *
   * @param format
   *          doc, dot, odt, html, pdf, ... or null to use the default format.
   */
  RemoteFile saveAs(String name, String format) throws ProcessingException;

  /**
   * @see IDocumentField#saveAs(String, String).
   * @param name
   * @return
   * @throws ProcessingException
   */
  RemoteFile saveAs(String name) throws ProcessingException;

  RemoteFile save() throws ProcessingException;

  void autoResizeDocument();

  IDocumentFieldUIFacade getUIFacade();
}
