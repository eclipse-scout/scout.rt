package org.eclipse.scout.migration.ecma6.task;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.AppNameContextProperty;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(300)
public class T300_HtmlStylesheetTags extends AbstractTask {
  private static final Logger LOG = LoggerFactory.getLogger(T300_HtmlStylesheetTags.class);

  private static final PathMatcher FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:src/main/resources/WebContent/{index,login,logout,popup-window,spnego_401,office-addin}.html");
  private Set<String> m_stylesheetsToRemove = CollectionUtility.hashSet("res/scout-login-module.less", "res/scout-logout-module.less", "res/scout-module.less");

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return FILE_MATCHER.matches(pathInfo.getModuleRelativePath());
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    m_stylesheetsToRemove.add("res/" + context.getProperty(AppNameContextProperty.class) + "-all-macro.less");
    WorkingCopy workingCopy = context.ensureWorkingCopy(pathInfo.getPath());
    removeStylesheetElements(workingCopy);
    addStylesheetElements(workingCopy, context);
  }

  private void removeStylesheetElements(WorkingCopy workingCopy) {
    String source = workingCopy.getSource();
    Document doc = Jsoup.parse(source);
    Elements scoutStylesheetElements = doc.getElementsByTag("scout:stylesheet");
    for (Element element : scoutStylesheetElements) {
      String attrSource = element.attr("src");
      if (m_stylesheetsToRemove.contains(attrSource)) {
        source = removeElement(element, source, workingCopy);
      }
    }
    workingCopy.setSource(source);
  }

  private String removeElement(Element element, String source, WorkingCopy workingCopy) {
    Pattern p = Pattern.compile("(\\s*)" + Pattern.quote(element.outerHtml()));
    Matcher removeTagMatcher = p.matcher(source);
    if (removeTagMatcher.find()) {
      source = removeTagMatcher.replaceAll("");
    }
    else {
      source = MigrationUtility.prependTodo(source, "remove style tag: '" + element.outerHtml() + "'", workingCopy.getLineDelimiter());
      LOG.warn("Could not remove style tag '" + element.outerHtml() + "' in '" + workingCopy.getPath() + "'");
    }
    return source;
  }

  private void addStylesheetElements(WorkingCopy workingCopy, Context context) {
    String source = workingCopy.getSource();
    workingCopy.getLineDelimiter();

    Document doc = Jsoup.parse(source);
    Elements scoutStylesheetElements = doc.getElementsByTag("scout:stylesheet");
    Element lastStyleSheet = scoutStylesheetElements.first();
    if (lastStyleSheet != null) {
      // append first
      Pattern pattern = Pattern.compile("(\\s*)" + Pattern.quote(lastStyleSheet.outerHtml()));
      Matcher matcher = pattern.matcher(source);
      //noinspection ResultOfMethodCallIgnored
      matcher.find();
      source = matcher.replaceAll(matcher.group(1) + lastStyleSheet.outerHtml() + createSource(matcher.group(1), context));
    }
    else {
      // before </head>
      Pattern pattern = Pattern.compile("(\\s*)(</head>)");
      Matcher matcher = pattern.matcher(source);
      if (matcher.find()) {
        source = matcher.replaceAll(createSource(matcher.group(1) + "  ", context) + matcher.group(1) + matcher.group(2));
      }
      else {
        source = MigrationUtility.prependTodo(source, "add stylesheet (<scout:stylesheet src=\"" + context.getProperty(AppNameContextProperty.class) + "-theme.css\" />", workingCopy.getLineDelimiter());
        LOG.warn("Could not add stylesheet (<scout:stylesheet src=\\\"\"+context.getProperty(AppNameContextProperty.class)+\"-theme.css\\\" /> in '" + workingCopy.getPath() + "'");
      }
    }
    workingCopy.setSource(source);
  }

  private String createSource(String newLineAndIndent, Context context) {
    String scriptBuilder = newLineAndIndent +
        "<scout:stylesheet src=\"" + context.getProperty(AppNameContextProperty.class) + "-theme.css\\\" />";
    return scriptBuilder;
  }
}
