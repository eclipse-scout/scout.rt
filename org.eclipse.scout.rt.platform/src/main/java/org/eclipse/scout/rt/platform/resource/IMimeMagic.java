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

import org.eclipse.scout.rt.platform.security.MalwareScanner;
import org.eclipse.scout.rt.platform.util.HexUtility;

/**
 * Typically used in combination with {@link MalwareScanner}
 * <p>
 * see {@link MimeTypes#verifyMagic(BinaryResource)} and https://en.wikipedia.org/wiki/List_of_file_signatures
 *
 * @since 10.x
 */
@FunctionalInterface
public interface IMimeMagic {
  IMimeMagic DOC_XLS_PPT = createMagic(0, "d0cf11e0a1b11ae1");
  IMimeMagic BMP = createMagic(0, "424d");
  IMimeMagic EXE_DLL_SYS = createMagic(0, "4d5a", "5a4d");
  IMimeMagic GIF = createMagic(0, "474946383761", "474946383961");
  IMimeMagic GZ = createMagic(0, "1f8b");
  IMimeMagic ICO = createMagic(0, "00000100");
  IMimeMagic JPEG_JPG = createMagic(0, "ffd8ffdb", "ffd8ffe0", "ffd8ffe1", "ffd8ffe2", "ffd8ffee");
  IMimeMagic MKV = createMagic(0, "1a45dfa3");
  IMimeMagic MP3 = createMagic(0, "494433", "fff2", "fff3", "fffb");
  IMimeMagic MP4 = createMagic(4, "6674797069736f6d", "667479706D703432");
  IMimeMagic MSG = createMagic(0, "2320637265617465", "6e616d6573706163", "d0cf11e0a1b11ae1");
  IMimeMagic PDF = createMagic(0, "25504446");
  IMimeMagic PNG = createMagic(0, "89504e470d0a1a0a");
  IMimeMagic TIF_TIFF = createMagic(0, "49492a00", "4d4d002a");
  IMimeMagic WOFF = createMagic(0, "774f4646", "774f4632");
  IMimeMagic ZIP = createMagic(0, "504b0304", "504b0506", "504b0708");

  /**
   * Validate file content or {@link BinaryResource}. Check headers and content in order to find out if the file is
   * valid or corrupt or malware
   *
   * @return true if the content of this resource complies with the mime type
   */
  boolean matches(BinaryResource res);

  static IMimeMagic createMagic(int pos, String... hexMagics) {
    byte[][] magics = new byte[hexMagics.length][];
    for (int i = 0; i < hexMagics.length; i++) {
      magics[i] = HexUtility.decode(hexMagics[i]);
    }
    return res -> {
      byte[] content = res.getContent();
      if (content == null) {
        return false;
      }
      for (byte[] magic : magics) {
        if (content.length < pos + magic.length) {
          continue;
        }
        boolean match = true;
        for (int i = 0; i < magic.length; i++) {
          if (content[pos + i] != magic[i]) {
            match = false;
            break;
          }
        }
        if (match) {
          return true;
        }
      }
      return false;
    };
  }
}
