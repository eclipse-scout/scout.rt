package org.eclipse.scout.migration.ecma6.task;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.AppNameContextProperty;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(200)
public class T200_IndexHtmlScriptTags extends AbstractTask{
  private static Path FILE_PATH = Paths.get("src/main/resources/WebContent/index.html");

  private static Pattern END_BODY_REGEX = Pattern.compile("(\\s*)\\<\\/body\\>");
  private static Pattern APP_NAME_REGEX = Pattern.compile("res\\/([^\\-]*)\\-all\\-macro\\.js");


  private Set<String> m_scriptSourcesToRemove = CollectionUtility.hashSet(
    "res/jquery-all-macro.js",
    "res/libs-all-macro.js",
    "res/index.js"
  ) ;

  private String m_newLineAndIndet = System.lineSeparator()+"    ";
  private List<String> m_scriptsMoveToBody = new ArrayList<>();

  @Override
  public boolean accept(Path file, Context context) {
    return file.endsWith(FILE_PATH);
  }

  @Override
  public void process(Path file, Context context) {
    WorkingCopy workingCopy = context.ensureWorkingCopy(file);
    removeScriptElements(workingCopy);
    addScriptElements(workingCopy,context);
  }

  private void removeScriptElements(WorkingCopy workingCopy){
    String source = workingCopy.getSource();
    Document doc = Jsoup.parse(source);
    Elements scoutScriptElements = doc.getElementsByTag("scout:script");
    for (Element element : scoutScriptElements) {
      // try to find appname
      String attrSource = element.attr("src");
      source = removeElement(element, source);
    }

    Elements scriptElements = doc.getElementsByTag("script");
    for (Element element : scriptElements) {
      String attrSource = element.attr("src");
      if(attrSource != null && attrSource.toLowerCase().endsWith(".js")){
        m_scriptsMoveToBody.add(element.outerHtml());
        source = removeElement(element, source);
      }
    }
    workingCopy.setSource(source);
  }

  private String removeElement(Element element, String source){
    Pattern p = Pattern.compile("(\\s*)"+Pattern.quote(element.outerHtml()));
    Matcher removeTagMatcher = p.matcher(source);
    if(removeTagMatcher.find()){
      m_newLineAndIndet = removeTagMatcher.group(1);
      source = removeTagMatcher.replaceAll("");
    }else{
      source = MigrationUtility.prependTodo(source, "remove script tag: '"+element.outerHtml()+"'");
    }
    return source;
  }


  private void addScriptElements(WorkingCopy workingCopy, Context context){
    String source = workingCopy.getSource();
    Matcher matcher = END_BODY_REGEX.matcher(source);
    if(matcher.find()){
      String bodyIndent = matcher.group(1);
      StringBuilder scriptBuilder = new StringBuilder();
      scriptBuilder
        .append(m_newLineAndIndet)
        .append("<scout:script src=\"jquery.js\" />")
        .append(m_newLineAndIndet)
        .append("<scout:script src=\"eclipse-scout.js\" />")
        .append(m_newLineAndIndet)
        .append("<scout:script src=\"").append(context.getProperty(AppNameContextProperty.class)).append(".js\" />");
      for(String scriptElement : m_scriptsMoveToBody){
        scriptBuilder.append(m_newLineAndIndet)
          .append(scriptElement);
      }

      scriptBuilder.append(bodyIndent)
        .append("</body>");
      source = matcher.replaceAll(scriptBuilder.toString());
      workingCopy.setSource(source);
    }else{
      MigrationUtility.prependTodo(workingCopy, "add script imports: <scout:script src=\"jquery.js\" /> " +
        "<scout:script src=\"eclipse-scout.js\" /> " +
        "<scout:script src=\"jswidgets.js\" />");
    }
  }
}
