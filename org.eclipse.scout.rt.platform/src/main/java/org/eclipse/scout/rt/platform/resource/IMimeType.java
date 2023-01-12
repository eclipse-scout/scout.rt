/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
