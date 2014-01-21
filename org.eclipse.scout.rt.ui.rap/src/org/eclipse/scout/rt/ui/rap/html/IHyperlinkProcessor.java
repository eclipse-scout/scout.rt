package org.eclipse.scout.rt.ui.rap.html;

public interface IHyperlinkProcessor {

  String processUrl(String url, boolean local);

  String processTarget(String target, boolean local);

}
