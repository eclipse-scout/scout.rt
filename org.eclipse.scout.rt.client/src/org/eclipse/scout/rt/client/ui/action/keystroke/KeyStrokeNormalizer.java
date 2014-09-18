/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.keystroke;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;

/**
 * This class parses and normalizes a Scout keystroke. A Scout keystroke is built from modifiers and a key. The
 * modifiers and the key are separated by a '-'. Examples:
 * <ul>
 * <li>control-alt-1
 * <li>control-shift-alt-1
 * <li>f11
 * <li>alt-f11
 * </ul>
 * The normalizer takes care that the keystroke is valid and follows the described pattern. The class is immutable.
 *
 * @since 5.0-M2
 */
public class KeyStrokeNormalizer {

  /**
   * the original Scout Keystroke passed to the constructor
   */
  private String m_originalKeyStroke;

  /**
   * defines if the normalized Keystroke has a SHIFT modifier
   */
  private boolean m_shift;

  /**
   * defines if the normalized Keystroke has a CTRL modifier
   */
  private boolean m_ctrl;

  /**
   * defines if the normalized Keystroke has a ALT modifier
   */
  private boolean m_alt;

  /**
   * holds the key of the normalized Keystroke
   */
  private String m_key;

  /**
   * holds the normalized Keystroke
   */
  private String m_normalizedKeyStroke;

  /**
   * defines if the Keystroke is valid
   */
  private boolean m_isValid = true;

  public KeyStrokeNormalizer(String keyStroke) {
    m_originalKeyStroke = keyStroke;
  }

  /**
   * This method parses and normalizes the Scout keystroke and sets the class' member variables.
   * Example: <code>
   * {@link KeyStrokeNormalizer} ks = new {@link KeyStrokeNormalizer}("ALT-shiFt-F11");<br>
   * ks.{@link #normalize()};<br>
   * <br>
   * ks.{@link #getNormalizedKeystroke()}; //returns 'shift-alternate-f11'<br>
   * ks.{@link #getKey()}; //returns 'f11' <br>
   * ks.{@link #hasShift()}; //returns 'true'<br>
   * ks.{@link #hasAlt()}; //returns 'true' <br>
   * ks.{@link #hasCtrl()}; //returns 'false'<br>
   * ks.{@link #isValid()}; //returns 'true' <br>
   * </code>
   * <br>
   * If the keystroke is invalid, {@link #isValid()} will return <code>false</code>, all modifiers ({@link #hasAlt()},
   * {@link #hasCtrl()}, {@link #hasShift()}) will be <code>false</code> and the
   * <code>{@link #getNormalizedKeystroke()} will return <code>null</code>
   */
  public void normalize() {
    String keyStroke = m_originalKeyStroke;
    if (StringUtility.hasText(keyStroke)) {
      keyStroke = keyStroke.toLowerCase();
      List<String> components = getComponents(keyStroke);
      if (components.size() > 1 && keyStroke.endsWith("-") && !CollectionUtility.lastElement(components).equals("-")) {
        setInvalid();
        return;
      }

      Iterator<String> iter = components.iterator();
      while (iter.hasNext()) {
        String currentComponent = iter.next();
        if ("shift".equalsIgnoreCase(currentComponent)) {
          m_shift = true;
        }
        else if ("control".equalsIgnoreCase(currentComponent)) {
          m_ctrl = true;
        }
        else if ("ctrl".equalsIgnoreCase(currentComponent)) {
          m_ctrl = true;
        }
        else if ("strg".equalsIgnoreCase(currentComponent)) {
          m_ctrl = true;
        }
        else if ("alt".equalsIgnoreCase(currentComponent)) {
          m_alt = true;
        }
        else if ("alternate".equalsIgnoreCase(currentComponent)) {
          m_alt = true;
        }
        else {
          if (!iter.hasNext()) {
            m_key = currentComponent;
          }
          else {
            setInvalid();
            return;
          }
        }
      }
      if (m_key != null) {
        m_normalizedKeyStroke = (m_shift ? "shift-" : "") + (m_ctrl ? "control-" : "") + (m_alt ? "alternate-" : "") + m_key;
      }
      else {
        setInvalid();
        return;
      }
    }
  }

  private List<String> getComponents(String keyStroke) {
    String[] components = keyStroke.trim().split("\\b-|-\\b");
    return CollectionUtility.arrayList(components);
  }

  private void setInvalid() {
    m_normalizedKeyStroke = null;
    m_alt = false;
    m_shift = false;
    m_ctrl = false;
    m_isValid = false;
  }

  /**
   * returns <code>true</code> if the Scout keystroke has the Shift modifier, <code>false</code> otherwise. <br>
   * Example: If the Scout keystroke is defined as <code>alt-control-f11</code>, {@link #hasShift()} will return
   * <code>false</code>
   */
  public boolean hasShift() {
    return m_shift;
  }

  /**
   * returns <code>true</code> if the Scout keystroke has the Ctrl modifier, <code>false</code> otherwise. <br>
   * Example: If the Scout keystroke is defined as <code>alt-control-f11</code>, {@link #hasCtrl()} will return
   * <code>true</code>
   */
  public boolean hasCtrl() {
    return m_ctrl;
  }

  /**
   * returns <code>true</code> if the Scout keystroke has the Alt modifier, <code>false</code> otherwise. <br>
   * Example: If the Scout keystroke is defined as <code>alt-control-f11</code>, {@link #hasAlt()} will return
   * <code>true</code>
   */
  public boolean hasAlt() {
    return m_alt;
  }

  /**
   * returns the key of the Scout keystroke. <br>
   * Example: If the Scout keystroke is defined as <code>alt-control-f11</code>, {@link #getKey()} will return
   * <code>f11</code>
   */
  public String getKey() {
    return m_key;
  }

  /**
   * returns the normalized version of the Scout keystroke. <br>
   * Examples:
   * <p>
   * <table>
   * <tr>
   * <th>Original Keystroke</th>
   * <th>Normalized Keystroke</th>
   * </tr>
   * <tr>
   * <td>F11</td>
   * <td>f11</td>
   * </tr>
   * <tr>
   * <td>ALT-SHIFT-F12</td>
   * <td>shift-alternate-f12</td>
   * </tr>
   * <tr>
   * <td>Strg-8</td>
   * <td>control-8</td>
   * </tr>
   * </table>
   * </p>
   */
  public String getNormalizedKeystroke() {
    return m_normalizedKeyStroke;
  }

  /**
   * returns <code>true</code> if the keystroke is valid, <code>false</code> otherwise
   */
  public boolean isValid() {
    return m_isValid;
  }
}
