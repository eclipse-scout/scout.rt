package org.eclipse.scout.rt.client.officeonline.wopi;

public final class HexUtility {

  private HexUtility() {
  }

  public static byte[] decodeHex(String hex) {
    hex = hex.replaceAll("[^0-9a-fA-F]", "");
    byte[] bytes = new byte[hex.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
    }
    return bytes;
  }

  public static String encodeHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      String s = Integer.toHexString(((int) data[i]) & 0xff);
      if (s.length() < 2) {
        buf.append("0");
      }
      buf.append(s);
    }
    return buf.toString();
  }
}
