package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IDocumentField extends IValueField<Object[][]> {

  String PROP_INSERT_TEXT = "insertText";
  String PROP_TOGGLE_RIBBONS = "toggleRibbons";
  String PROP_FILE = "file";
  String PROP_DISPLAY_RULERS = "displayRulers";
  String PROP_AUTORESIZE_DOCUMENT = "autoResizeDocument";
  String PROP_DISPLAY_STATUS_BAR = "displayStatusBar";

  public void insertText(String text);

  public void toggleRibbon();

  public void setFile(File file);

  public File getFile();

  public void setDisplayRulers(boolean displayRulers);

  public boolean isDisplayRulers();

  public void setAutoResizeDocument(boolean autoResizeDocument);

  public boolean isAutoResizeDocument();

  public void setDisplayStatusBar(boolean displayStatusBar);

  public boolean isDisplayStatusBar();
}
