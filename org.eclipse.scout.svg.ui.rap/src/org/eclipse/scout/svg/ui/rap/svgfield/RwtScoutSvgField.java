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
package org.eclipse.scout.svg.ui.rap.svgfield;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.svg.client.svgfield.ISvgField;
import org.eclipse.scout.svg.ui.rap.AbstractRwtScoutSvgComposite;
import org.eclipse.swt.browser.LocationEvent;
import org.w3c.dom.svg.SVGDocument;

public class RwtScoutSvgField extends AbstractRwtScoutSvgComposite<ISvgField> implements IRwtScoutSvgField {

  private String m_currentLocation;

  public RwtScoutSvgField() {
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateSvgDocument();
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ISvgField.PROP_SVG_DOCUMENT)) {
      updateSvgDocument();
    }
  }

  @Override
  protected void locationChangedFromUi(final LocationEvent event) {
    try {
      final URL url = new URL(event.location);
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireHyperlinkFromUI(url);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
    catch (MalformedURLException e) {
      //nop
    }
  }

  @Override
  protected SVGDocument getSvgDocument() {
    return getScoutObject().getSvgDocument();
  }
}
