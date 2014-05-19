package org.eclipse.scout.rt.ui.html;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//FIXME seams to be very similar to IOUtility. Either use IOUtility or enhance IOUtility with missing functionality. Or maybe use FileUtility?
public final class TextFileUtil {

  private TextFileUtil() {
  }

  public static String readUTF8(URL url) throws IOException {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    byte[] buf = new byte[10240];
    InputStream in = url.openStream();
    try {
      int n;
      while ((n = in.read(buf, 0, buf.length)) > 0) {
        data.write(buf, 0, n);
      }
    }
    finally {
      in.close();
    }
    return new String(data.toByteArray(), "UTF-8");
  }

  public static String readUTF8(File file) throws IOException {
    if (!file.exists()) {
      throw new IOException("File does not exist: " + file.getAbsolutePath());
    }
    int len = (int) file.length();
    int pos = 0;
    byte[] data = new byte[len];
    FileInputStream in = new FileInputStream(file);
    try {
      while (pos < len) {
        pos += in.read(data, pos, len - pos);
      }
    }
    finally {
      in.close();
    }
    return new String(data, "UTF-8");
  }

  public static void writeUTF8(File file, String content) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    try {
      out.write(content.getBytes("UTF-8"));
    }
    finally {
      out.close();
    }
  }

  private static final Pattern INCLUDE_PAT = Pattern.compile("//\\s*@include\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

  /**
   * @param content
   * @param rootDirForIncludes
   */
  public static String processIncludeDirectives(String content, ITextFileLoader loader) throws IOException {
    StringBuilder buf = new StringBuilder();
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      String path = mat.group(1);
      buf.append(loader.read(path));
      pos = mat.end();
    }
    buf.append(content.substring(pos));
    return buf.toString();
  }
}
