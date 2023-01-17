/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.client;

import java.awt.EventQueue;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;

/**
 * Workaround subclass for {@link JSVGCanvas} to fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=362466
 */
public class JSVGCanvasEx extends JSVGCanvas {

  private static final long serialVersionUID = 1L;

  public JSVGCanvasEx(SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
    super(ua, eventsEnabled, selectableText);
    toolTipListener = new ToolTipModifierEx();
  }

  protected class ToolTipModifierEx implements EventListener {

    @Override
    public void handleEvent(Event evt) {
      if (matchLastToolTipEvent(evt.getTimeStamp(), evt.getTarget())) {
        return;
      }
      setLastToolTipEvent(evt.getTimeStamp(), evt.getTarget());
      EventTarget prvLastTarget = lastTarget;
      if (SVGConstants.SVG_EVENT_MOUSEOVER.equals(evt.getType())) {
        lastTarget = evt.getTarget();
      }
      else if (SVGConstants.SVG_EVENT_MOUSEOUT.equals(evt.getType())) {
        MouseEvent e = ((MouseEvent) evt);
        lastTarget = e.getRelatedTarget();
      }
      if (toolTipMap != null) {
        Element e = (Element) lastTarget;
        Object ttText = null;
        while (e != null) {
          ttText = toolTipMap.get(e);
          if (ttText != null) {
            break;
          }
          e = CSSEngine.getParentCSSStylableElement(e);
        }
        final String tt = (String) ttText;
        if (prvLastTarget != lastTarget) {
          EventQueue.invokeLater(new ToolTipRunnable(tt));
        }
      }
    }
  }
}
