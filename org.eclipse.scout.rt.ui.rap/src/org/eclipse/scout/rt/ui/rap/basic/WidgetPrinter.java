/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import java.io.File;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;

public class WidgetPrinter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WidgetPrinter.class);

  private Control m_uiWidget;

  public WidgetPrinter(Control uiWidget) {
    m_uiWidget = uiWidget;
  }

  public void print(PrintDevice device, Map<String, Object> parameters) throws Throwable {
    //XXX rap
    /*
    if (device == PrintDevice.File) {
      printToFile(createImage(), parameters);
    }
    else if (device == PrintDevice.Printer) {
      printToPrinter(createImage(), parameters);
    }
    */
  }

  private void printToFile(Image image, Map<String, Object> parameters) throws Throwable {
    File file = (File) parameters.remove("file");
    if (file == null) {
      throw new IllegalArgumentException("parameter \"file\" must not be null");
    }
    String contentType = (String) parameters.remove("contentType");
    if (contentType == null) {
      contentType = "image/jpg";
    }
    if (!contentType.startsWith("image/")) {
      throw new IllegalArgumentException("only supporting contentTypes image/*");
    }
    for (String n : parameters.keySet()) {
      LOG.warn("Unknown parameter: " + n + "=" + parameters.get(n));
    }
    //
    File tmpFile = new File(file.getAbsolutePath() + ".tmp");
    tmpFile.getParentFile().mkdirs();
    //
    ImageLoader imageLoader = new ImageLoader();
    imageLoader.data = new ImageData[]{image.getImageData()};
    imageLoader.save(tmpFile.getAbsolutePath(), SWT.IMAGE_JPEG);
    if (image != null && !image.isDisposed() && image.getDevice() != null) {
      image.dispose();
    }
    file.delete();
    tmpFile.renameTo(file);
  }

//XXX rap
  /*
  private void printToPrinter(Image image, Map<String, Object> parameters) throws Throwable {
    @SuppressWarnings("unused")
    String printerName = (String) parameters.remove("printerName");
    String jobName = (String) parameters.remove("jobName");
    for (String n : parameters.keySet()) {
      LOG.warn("Unknown parameter: " + n + "=" + parameters.get(n));
    }
    Shell shell = new Shell();
    PrintDialog dialog = new PrintDialog(shell, SWT.NONE);
    PrinterData pdata = dialog.open();
    if (pdata != null) {
      Printer printer = new Printer(pdata);
      try {
        if (printer.startJob(jobName != null ? jobName : "Widget Screenshot")) {
          try {
            new DefaultPrintable().print(printer, image);
          }
          finally {
            printer.endJob();
          }
        }
      }
      finally {
        printer.dispose();
      }
    }
  }*/

  /*
   * //XXX rap
  private Image createImage() {
    Rectangle bounds = m_uiWidget.getBounds();
    int x = 0;
    int y = 0;
    if (m_uiWidget instanceof Shell) {
      Rectangle ca = ((Shell) m_uiWidget).getClientArea();
      x = (bounds.width - ca.width) / 2;
      y = (bounds.height - ca.height) - x;
    }
    GC gc = new GC(m_uiWidget);
    Image image = new Image(m_uiWidget.getDisplay(), bounds.width, bounds.height);
    gc.copyArea(image, -x, -y);
    gc.dispose();
    return image;
  }*/
/*
 *
  private class DefaultPrintable {
    public void print(Printer printer, Image image) {
      if (printer.startPage()) {
        GC gc = new GC(printer);
        try {
          System.out.println("ok");
          Rectangle clientArea = printer.getClientArea();
          Rectangle trim = printer.computeTrim(0, 0, 0, 0);
          Point dpi = printer.getDPI();
          int leftMargin = 0 * dpi.x + (-trim.x);
          int rightMargin = 0 * dpi.x + (trim.x + trim.width);
          int topMargin = 0 * dpi.y + (-trim.y);
          int bottomMargin = 0 * dpi.y + (trim.y + trim.height);
          Rectangle drawBounds = new Rectangle(leftMargin, topMargin, clientArea.width - leftMargin - rightMargin, clientArea.height - topMargin - bottomMargin);
          Rectangle widgetBounds = m_uiWidget.getBounds();
          //
          double scaleX = 1.0 * drawBounds.width / Math.max(1, widgetBounds.width);
          double scaleY = 1.0 * drawBounds.height / Math.max(1, widgetBounds.height);
          double scaleProp = Math.min(scaleX, scaleY);
          gc.drawImage(image, 0, 0, widgetBounds.width, widgetBounds.height, drawBounds.x, drawBounds.y, (int) (widgetBounds.width * scaleProp), (int) (widgetBounds.height * scaleProp));
        }
        finally {
          gc.dispose();
          printer.endPage();
        }
      }
    }
  }
*/
}
