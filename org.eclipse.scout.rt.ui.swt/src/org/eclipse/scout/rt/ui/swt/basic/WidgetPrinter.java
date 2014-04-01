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
package org.eclipse.scout.rt.ui.swt.basic;

import java.io.File;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WidgetPrinter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WidgetPrinter.class);

  private Control m_widget;

  private File m_printedFile;

  public WidgetPrinter(Control w) {
    m_widget = w;
  }

  public void print(PrintDevice device, Map<String, Object> parameters) throws Throwable {
    Image image = createImage();
    try {
      if (device == PrintDevice.File) {
        printToFile(image, parameters);
      }
      else if (device == PrintDevice.Printer) {
        printToPrinter(image, parameters);
      }
    }
    finally {
      image.dispose();
    }
  }

  public File getOutputFile() {
    return m_printedFile;
  }

  protected void printToFile(Image image, Map<String, Object> parameters) throws Throwable {
    m_printedFile = (File) parameters.remove("file");
    if (m_printedFile == null) {
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
    File tmpFile = new File(m_printedFile.getAbsolutePath() + ".tmp");
    tmpFile.getParentFile().mkdirs();
    //
    ImageLoader imageLoader = new ImageLoader();
    imageLoader.data = new ImageData[]{image.getImageData()};
    imageLoader.save(tmpFile.getAbsolutePath(), SWT.IMAGE_JPEG);
    image.dispose();
    m_printedFile.delete();
    tmpFile.renameTo(m_printedFile);
  }

  protected void printToPrinter(Image image, Map<String, Object> parameters) throws Throwable {
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
  }

  protected Image createImage() {
    Rectangle bounds = m_widget.getBounds();
    int width = bounds.width;
    int height = bounds.height;
    if (m_widget instanceof Shell) {
      Rectangle ca = ((Shell) m_widget).getClientArea();
      width = ca.width;
      height = ca.height;
    }

    GC gcWidget = new GC(m_widget);
    Image image = new Image(m_widget.getDisplay(), width, height);
    Image imageWithFrame;
    try {
      gcWidget.copyArea(image, 0, 0);
      int frameWidth = 2;
      imageWithFrame = createImageWithFrame(image, frameWidth);
    }
    finally {
      gcWidget.dispose();
      image.dispose();
    }

    return imageWithFrame;
  }

  protected Image createImageWithFrame(Image image, int frameWidth) {
    Image imageWithFrame = new Image(m_widget.getDisplay(), image.getBounds().width + (2 * frameWidth), image.getBounds().height + (2 * frameWidth));
    GC gcResult = new GC(imageWithFrame);
    try {
      gcResult.setAdvanced(true);
      gcResult.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
      gcResult.setLineWidth(frameWidth);
      gcResult.drawRectangle(imageWithFrame.getBounds());
      gcResult.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, frameWidth, frameWidth, image.getBounds().width, image.getBounds().height);
    }
    finally {
      gcResult.dispose();
    }
    return imageWithFrame;
  }

  protected class DefaultPrintable {
    public void print(Printer printer, Image image) {
      if (printer.startPage()) {
        GC gc = new GC(printer);
        try {
          Rectangle clientArea = printer.getClientArea();
          Rectangle trim = printer.computeTrim(0, 0, 0, 0);
          Point dpi = printer.getDPI();
          int leftMargin = 0 * dpi.x + (-trim.x);
          int rightMargin = 0 * dpi.x + (trim.x + trim.width);
          int topMargin = 0 * dpi.y + (-trim.y);
          int bottomMargin = 0 * dpi.y + (trim.y + trim.height);
          Rectangle drawBounds = new Rectangle(leftMargin, topMargin, clientArea.width - leftMargin - rightMargin, clientArea.height - topMargin - bottomMargin);
          Rectangle widgetBounds = m_widget.getBounds();
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

}
