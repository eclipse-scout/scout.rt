package org.eclipse.scout.rt.client.officeonline.wopi;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public final class WopiUtility {

  private WopiUtility() {
  }

  //{59CCD75F-0687-4F86-BBCF-059126640640},1
  public static String createWopiFileVersion(FileInfo fi) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    cal.setTimeInMillis(fi.getLastModified());
    long t = cal.getTimeInMillis();
    byte[] a = new byte[8];
    for (int i = 0; i < a.length; i++)
    {
      a[i] = (byte) (t & 0xff);
      t = t >> 8;
    }
    return "{00000000-0000-0000-0000-" + HexUtility.encodeHex(a) + "},1";
  }

  public static String createWopiLocale(Locale locale) {
    return locale.getLanguage() + "-" + locale.getCountry();//de-CH, en-US
  }
}
