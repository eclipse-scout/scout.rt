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
package org.eclipse.scout.rt.platform.nls;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Warning: Avoid osgi imports in this class, so it can be used in J2EE
 */
public final class NlsUtility {
  private static final Logger LOG = LoggerFactory.getLogger(NlsUtility.class);

  private NlsUtility() {
  }

  private static final int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;

  private static final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;

  public static void dynamicBindFields(Class<?> clazz) {
    final Field[] fieldArray = clazz.getDeclaredFields();

    boolean isAccessible = (clazz.getModifiers() & Modifier.PUBLIC) != 0;

    // build a map of field names to Field objects
    // final int len = fieldArray.length;

    for (Field field : fieldArray) {
      // can only set value of public static non-final fields
      if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED) {
        continue;
      }
      try {
        // Check to see if we are allowed to modify the field. If we
        // aren't (for instance
        // if the class is not public) then change the accessible
        // attribute of the field
        // before trying to set the value.
        if (!isAccessible) {
          makeAccessible(field);
        }
        // Set the value into the field. We should never get an
        // exception here because
        // we know we have a public static non-final field. If we do get
        // an exception, silently
        // log it and continue. This means that the field will (most
        // likely) be un-initialized and
        // will fail later in the code and if so then we will see both
        // the NPE and this error.
        field.set(null, field.getName());
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
  }

  /*
   * Change the accessibility of the specified field so we can set its value to
   * be the appropriate message string.
   */
  static void makeAccessible(final Field field) {
    if (System.getSecurityManager() == null) {
      field.setAccessible(true);
    }
    else {
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
          field.setAccessible(true);
          return null;
        }
      });
    }
  }

  private static Pattern messageArgumentPattern = Pattern.compile("\\{([0-9]+)\\}");

  /**
   * @param key
   *          nls text key
   * @param messageArguments
   *          the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *          with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *          with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *          getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *          nicht gefunden werden
   */
  public static String bindText(String text, String... messageArguments) {
    if (text != null) {
      // check potential for message arguments
      if (messageArguments != null && messageArguments.length > 0) {
        Matcher m = messageArgumentPattern.matcher(text);
        StringBuilder b = new StringBuilder();
        int start = 0;
        while (m.find(start)) {
          b.append(text.substring(start, m.start()));
          int index = Integer.parseInt(m.group(1));
          if (index < messageArguments.length) {
            if (messageArguments[index] != null) {
              b.append(messageArguments[index]);
            }
          }
          else {
            b.append("{" + index + "}");
          }
          // next
          start = m.end();
        }
        b.append(text.substring(start));
        return b.toString();
      }
      else {
        return text;
      }
    }
    return text;
  }

  /**
   * Only use this {@link Locale} resolver if the calling code is executed in both, server- and client side. On client
   * side, use {@link Locale#getDefault()} whereas on server side use {@link NlsLocale#get()}.
   *
   * @return the locale hold by the current thread in {@link NlsLocale} or if not set by the instance of the Java
   *         Virtual Machine.
   */
  public static Locale getDefaultLocale() {
    Locale locale = NlsLocale.get();
    if (locale == null) {
      locale = Locale.getDefault();
    }
    return locale;
  }

}
