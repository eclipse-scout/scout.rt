package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;

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
   * save the document content and updates the new value (RemoteFile) of this document field
   * <p>
   * This method will call {@link #saveAs(File, String)}
   * <p>
   * When the save of the document (for example in format type html) produces multiple files, then the created
   * RemoteFile contains compressed data (*.zip)
   * <p>
   * Note that this call is waiting for the producing of the file and synchronously completes
   * 
   * @param formatType
   *          doc, dot, odt, html, pdf, ...
   * @param timeout
   *          milliseconds to wait for document production until a interruption error occurs
   */
  RemoteFile saveAs(String name, String formatType, long timeout) throws ProcessingException;

  void autoResizeDocument();

  IDocumentFieldUIFacade getUIFacade();
}
