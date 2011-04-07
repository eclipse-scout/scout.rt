/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.calendar;

import java.util.HashMap;
import java.util.Map.Entry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * SWT Color Management
 *
 * @author Michael Rudolf, Andreas Hoegger
 *
 */
public class SwtColors {

  public final Color white;
  public final Color lightgray;
  public final Color gray;
  public final Color blue;
  public final Color darkgray;
  public final Color red;
  public final Color black;
  public final Color widget_background;

  private HashMap<String, Color> m_registry = new HashMap<String, Color>();
  private HashMap<Integer, Color> m_scoutColorRegistry = new HashMap<Integer, Color>();

  protected static SwtColors instance = null;

  protected Display display;

  protected SwtColors () {
    display = Display.getDefault();
//     display.addListener(SWT.Dispose,new Listener(){
//       public void handleEvent(Event event) {
//        dispose();
//      }
//     });
    white = display.getSystemColor(SWT.COLOR_WHITE);

    lightgray = new Color (display, 240, 240, 240);
    blue = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
    gray = display.getSystemColor(SWT.COLOR_GRAY);
    darkgray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
    red = display.getSystemColor(SWT.COLOR_RED);
    black = display.getSystemColor(SWT.COLOR_BLACK);
     widget_background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
  }

  public static SwtColors getInstance () {
    if (instance == null)
      instance = new SwtColors ();

    return instance;
  }

  /** get a new SWT color from an RGB value */
  public Color getColor (RGB rgb) {
    // get color
    Color c = m_registry.get(rgb.toString());
    if(c == null){
      c = new Color (display, rgb);
      m_registry.put(rgb.toString(),c);
    }
    return c;
  }

  public static Display getStandardDisplay() {
    Display display = Display.getCurrent();
    if (display == null) {
      display = Display.getDefault();
    }
    return display;
  }

  /** get a new SWT color from an scout one */
  public Color getColor(String c) {
    int i = Integer.parseInt(c, 16);

    Color col = m_scoutColorRegistry.get(i);
    if(col == null){
      col = new Color(getStandardDisplay(),((i >> 16) & 0xFF),((i >> 8) & 0xFF),((i >> 0) & 0xFF));
      m_scoutColorRegistry.put(i, col);
    }
    return col;
  }

  /** return a color darker than the input c (ratio 0.75) */
  public Color getDarker (Color c) {
    return getDarker (c, 0.75F);
  }

  /** return a color darker than the input c by the given ratio */
  public Color getDarker (Color c, float ratio) {
    int r = (int) (c.getRed() * ratio);
    int g = (int) (c.getGreen() * ratio);
    int b = (int) (c.getBlue() * ratio);
    return getColor (new RGB(r,g,b));
  }

  /** dispose all allocated colors */
  public void dispose ( ) {
    for(Entry<String, Color> entry :m_registry.entrySet()){
      entry.getValue().dispose();
    }
    for(Entry<Integer, Color> entry :m_scoutColorRegistry.entrySet()){
      entry.getValue().dispose();
    }
    m_registry.clear();
    m_scoutColorRegistry.clear();
  }

}
