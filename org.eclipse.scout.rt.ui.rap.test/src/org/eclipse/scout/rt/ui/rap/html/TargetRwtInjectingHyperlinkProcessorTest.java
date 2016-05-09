/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.html;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class TargetRwtInjectingHyperlinkProcessorTest {

  private void runConvert(String origString, String expected) {
    runConvert(origString, expected, null);
  }

  private void runConvert(String origString, String expected, Map<String, String> params) {
    HyperlinkParser parser = new HyperlinkParser();
    TargetRwtInjectingHyperlinkProcessor processor = new TargetRwtInjectingHyperlinkProcessor(params);
    String result = parser.parse(origString, processor);
    assertEquals(expected, result);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellSimple() {
    String origString = "<html><a href=\"http://local/xy\">link</a></html>";
    String expectedString = "<html><a href=\"http://local/xy\" target=\"_rwt\">link</a></html>";

    runConvert(origString, expectedString);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellWhitespaces() {
    String origString = "<html><a   href=\"http://local/xy\">link</a></html>";
    String expectedString = "<html><a   href=\"http://local/xy\" target=\"_rwt\">link</a></html>";

    runConvert(origString, expectedString);
  }

  @Test
  public void testConvertLinksWithLocalUrlsBold() {
    String origString = "<html><a   href=\"http://local/xy\"><b>link</b></a></html>";
    String expectedString = "<html><a   href=\"http://local/xy\" target=\"_rwt\"><b>link</b></a></html>";

    runConvert(origString, expectedString);
  }

  @Test
  public void testConvertLinksWithLocalUrlsExistingTarget() {
    String origString = "<html><a   href=\"http://local/xy\" target=\"_blank\"><b>link</b></a></html>";
    String expectedString = "<html><a   href=\"http://local/xy\" target=\"_rwt\"><b>link</b></a></html>";

    runConvert(origString, expectedString);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellParams() {
    String origString = "<html><a href=\"http://local/xy?param=1\">link</a></html>";
    String expectedString = "<html><a href=\"http://local/xy?param=1&amp;paramAdded=3\" target=\"_rwt\">link</a></html>";
    Map<String, String> params = new HashMap<String, String>();
    params.put("paramAdded", "3");

    runConvert(origString, expectedString, params);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellParamsMultiple() {
    String origString = "<html><a href=\"http://local/xy?param=1&param=2\">link</a></html>";
    String expectedString = "<html><a href=\"http://local/xy?param=1&param=2&amp;paramAdded=3&amp;paramAdded2=4\" target=\"_rwt\">link</a></html>";
    Map<String, String> params = new TreeMap<String, String>();
    params.put("paramAdded", "3");
    params.put("paramAdded2", "4");

    runConvert(origString, expectedString, params);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellParamsSpecialChars() {
    String origString = "<html><a href=\"http://local/xy&#63;param=1&amp;param=2\">link</a></html>";
    String expectedString = "<html><a href=\"http://local/xy&#63;param=1&amp;param=2&amp;paramAdded=3\" target=\"_rwt\">link</a></html>";
    Map<String, String> params = new HashMap<String, String>();
    params.put("paramAdded", "3");

    runConvert(origString, expectedString, params);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellParamsMobile() {
    String origString = "<html>" +
        "<table width=\"100%\" height=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"color:inherit;background-color:inherit;font-size:14px;table-layout:fixed;border-collapse: collapse;\">" +
        "  <colgroup>" +
        "    <col width=\"6/\">" +
        "    <col />" +
        "    <col width=\"60/\"/>" +
        "  </colgroup>" +
        "  <tbody>" +
        "  <tr style=\"border-bottom:1px solid #e1efec\">" +
        "    <td align=\"center\"></td>" +
        "    <td style=\"text-overflow:ellipsis;overflow:hidden;white-space:nowrap;\">Ferienadresse<br/></td>" +
        "    <td><a" +
        "  href=\"http://local/scout?action=drill_down\"" +
        "  style=\"" +
        "    display:block;" +
        "    background-image:url('rwt-resources/generated/17391170.png');" +
        "    background-repeat:no-repeat;" +
        "    background-position:center;" +
        "    width:100%;height:50px;\"" +
        "  onclick=\"currentElement=event.target || event.srcElement;currentElement.style.backgroundImage='url(rwt-resources/generated/4c07c313.png)';return false;\">" +
        "</a>" +
        "</td>" +
        "  </tr>" +
        "  </tbody>" +
        "</table>" +
        "</html>";

    String expectedString = "" +
        "<html>" +
        "<table width=\"100%\" height=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"color:inherit;background-color:inherit;font-size:14px;table-layout:fixed;border-collapse: collapse;\">" +
        "  <colgroup>" +
        "    <col width=\"6/\">" +
        "    <col />" +
        "    <col width=\"60/\"/>" +
        "  </colgroup>" +
        "  <tbody>" +
        "  <tr style=\"border-bottom:1px solid #e1efec\">" +
        "    <td align=\"center\"></td>" +
        "    <td style=\"text-overflow:ellipsis;overflow:hidden;white-space:nowrap;\">Ferienadresse<br/></td>" +
        "    <td><a" +
        "  href=\"http://local/scout?action=drill_down&amp;1row1Num1=0\"" +
        "  style=\"" +
        "    display:block;" +
        "    background-image:url('rwt-resources/generated/17391170.png');" +
        "    background-repeat:no-repeat;" +
        "    background-position:center;" +
        "    width:100%;height:50px;\"" +
        "  onclick=\"currentElement=event.target || event.srcElement;currentElement.style.backgroundImage='url(rwt-resources/generated/4c07c313.png)';return false;\" target=\"_rwt\">" +
        "</a>" +
        "</td>" +
        "  </tr>" +
        "  </tbody>" +
        "</table>" +
        "</html>";

    Map<String, String> params = new HashMap<String, String>();
    params.put("1row1Num1", "0");

    runConvert(origString, expectedString, params);
  }

  @Test
  public void testConvertLinksWithLocalUrlsInHtmlCellPhone() {
    String origString = "" +
        "<html><head><style>a{color:#67a8ce;}</style></head>" +
        "<body>" +
        "<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" style=\"color:inherit;background-color:inherit;font-size:12px;table-layout:fixed;width:100%;padding:3px 3px 0px 5px;\">" +
        "<tr><td><a href=\"http://local/domain?key0=204128502&amp;domainUid=318596\">Hans Meier</a></td></tr>" +
        "</table>" +
        "<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" style=\"color:inherit;background-color:inherit;font-size:12px;table-layout:fixed;width:100%;padding:0px 3px 0px 5px;\">" +
        "<tr><td>&nbsp;&nbsp;Intern</td><td align=\"right\">18.02.13&nbsp;14:49</td></tr>" +
        "<tr><td>&nbsp;&nbsp;817</td><td align=\"right\">(Gewählt)</td></tr>" +
        "</table>" +
        "</body></html>";

    String expectedString = "" +
        "<html><head><style>a{color:#67a8ce;}</style></head>" +
        "<body>" +
        "<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" style=\"color:inherit;background-color:inherit;font-size:12px;table-layout:fixed;width:100%;padding:3px 3px 0px 5px;\">" +
        "<tr><td><a href=\"http://local/domain?key0=204128502&amp;domainUid=318596\" target=\"_rwt\">Hans Meier</a></td></tr></table>" +
        "<table cellspacing=\"0\" cellpadding=\"1\" border=\"0\" style=\"color:inherit;background-color:inherit;font-size:12px;table-layout:fixed;width:100%;padding:0px 3px 0px 5px;\">" +
        "<tr><td>&nbsp;&nbsp;Intern</td><td align=\"right\">18.02.13&nbsp;14:49</td></tr>" +
        "<tr><td>&nbsp;&nbsp;817</td><td align=\"right\">(Gewählt)</td></tr>" +
        "</table>" +
        "</body></html>";

    runConvert(origString, expectedString);
  }

}
