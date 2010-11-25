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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.RepaintManager;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;

public class WidgetPrinter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WidgetPrinter.class);

  private Component m_widget;

  public WidgetPrinter(Component comp) {
    m_widget = comp;
  }

  public void print(PrintDevice device, Map<String, Object> parameters) throws Throwable {
    if (device == PrintDevice.Printer) {
      printToPrinter(parameters);
    }
    else if (device == PrintDevice.File) {
      printToFile(parameters);
    }
  }

  private void printToPrinter(Map<String, Object> parameters) throws Throwable {
    @SuppressWarnings("unused")
    String printerName = (String) parameters.remove("printerName");
    String jobName = (String) parameters.remove("jobName");
    for (String n : parameters.keySet()) {
      LOG.warn("Unknown parameter: " + n + "=" + parameters.get(n));
    }
    //
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setJobName(jobName != null ? jobName : "Widget Screenshot");
    if (job.printDialog()) {
      job.setPrintable(new DefaultPrintable());
      job.print();
    }
  }

  private void printToFile(Map<String, Object> parameters) throws Throwable {
    File file = (File) parameters.remove("file");
    if (file == null) throw new IllegalArgumentException("parameter \"file\" must not be null");
    String contentType = (String) parameters.remove("contentType");
    if (contentType == null) contentType = "image/jpg";
    if (!contentType.startsWith("image/")) throw new IllegalArgumentException("only supporting contentTypes image/*");
    for (String n : parameters.keySet()) {
      LOG.warn("Unknown parameter: " + n + "=" + parameters.get(n));
    }
    //
    file.getParentFile().mkdirs();
    BufferedImage img = createBufferedImage();
    String imageFormat = contentType.substring(contentType.indexOf("/") + 1);
    boolean ok = ImageIO.write(img, imageFormat, file);
    if (!ok) throw new IOException("no appropriate writer was found for imageFormat \"" + imageFormat + "\"");
  }

  private BufferedImage createBufferedImage() {
    // print component to offscreen image
    BufferedImage img = new BufferedImage(m_widget.getWidth(), m_widget.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics gOff = img.getGraphics();
    gOff.setColor(Color.white);
    gOff.fillRect(0, 0, m_widget.getWidth(), m_widget.getHeight());
    try {
      RepaintManager.currentManager(m_widget).setDoubleBufferingEnabled(false);
      m_widget.printAll(gOff);
    }
    finally {
      RepaintManager.currentManager(m_widget).setDoubleBufferingEnabled(true);
    }
    gOff.dispose();
    return img;
  }

  private class DefaultPrintable implements Printable, ImageObserver {
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
      if (pageIndex > 0) {
        return NO_SUCH_PAGE;
      }
      BufferedImage img = createBufferedImage();
      // scaling
      Graphics2D g2d = (Graphics2D) g;
      Rectangle pageRect = g2d.getClipBounds();
      Dimension compSize = m_widget.getSize();
      double scaleX = 1.0 * pageRect.width / Math.max(1, compSize.width);
      double scaleY = 1.0 * pageRect.height / Math.max(1, compSize.height);
      double scaleProp = Math.min(scaleX, scaleY);
      // physical print
      g2d.translate(pageRect.x, pageRect.y);
      g2d.scale(scaleProp, scaleProp);
      g2d.drawImage(img, 0, 0, this);
      g2d.scale(1 / scaleProp, 1 / scaleProp);
      g2d.translate(-pageRect.x, -pageRect.y);
      return PAGE_EXISTS;
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return ((infoflags & (ImageObserver.ALLBITS | ImageObserver.FRAMEBITS)) != 0);
    }
  }

}
