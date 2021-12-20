/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.resource;

/**
 * Support for extensible {@link MimeType} enums
 */
public interface IMimeType {

  /**
   * @return the mime type or media type in the format majorPart/minorPart
   */
  String getType();

  default String getMajorPart() {
    String t = getType();
    int i = t.indexOf('/');
    return i > 0 ? t.substring(0, i) : "";
  }

  default String getMinorPart() {
    String t = getType();
    int i = t.indexOf('/');
    return i > 0 ? t.substring(i + 1) : "";
  }

  String getFileExtension();

  IMimeMagic getMagic();

  void setMagic(IMimeMagic magic);
}
