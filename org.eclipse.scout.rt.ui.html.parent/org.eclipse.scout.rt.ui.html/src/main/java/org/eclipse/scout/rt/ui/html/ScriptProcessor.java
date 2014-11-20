package org.eclipse.scout.rt.ui.html;

import java.io.File;
import java.io.IOException;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;

/**
 * Process JS and CSS scripts such as <code>/WebContent/res/scout-4.0.0.css</code> and
 * <code>/WebContent/res/scout-4.0.0.js</code>
 */
public class ScriptProcessor {

  public static void main(String[] args) throws IOException {
    File outputFile = null;
    ScriptProcessor processor = new ScriptProcessor();
    for (int i = 0; i < args.length; i++) {
      if ("--i".equals(args[i])) {
        String path = args[i + 1];
        processor.setInput(path, TextFileUtil.readUTF8(new File(path)));
      }
      if ("--o".equals(args[i])) {
        outputFile = new File(args[i + 1]);
      }
      if ("--root".equals(args[i])) {
        final File rootDir = new File(args[i + 1]);
        processor.m_includeFileLoader = new ITextFileLoader() {
          @Override
          public String read(String path) throws IOException {
            return TextFileUtil.readUTF8(new File(rootDir, path));
          }
        };
      }
    }
    if (processor.m_input == null) {
      usage("missing --i");
      System.exit(-1);
      return;
    }
    if (outputFile == null) {
      usage("missing --o");
      System.exit(-1);
      return;
    }
    if (processor.m_includeFileLoader == null) {
      usage("missing --root");
      System.exit(-1);
      return;
    }
    outputFile.getAbsoluteFile().getParentFile().mkdirs();
    TextFileUtil.writeUTF8(outputFile, processor.process());
  }

  public static void usage(String errorMessage) {
    if (errorMessage != null) {
      System.err.println("ERROR: " + errorMessage);
    }
    System.err.println("Usage: " + ScriptProcessor.class.getName() + " --i inputfilename --o outputfilename --root rootDirForIncludes");
  }

  private String m_input;
  private String m_inputType;
  private ITextFileLoader m_includeFileLoader;
  private boolean m_showLineNumbers = false;

  public void setInput(String path, String content) {
    int dot = path.lastIndexOf('.');
    m_inputType = path.substring(dot + 1);
    m_input = content;
  }

  public void setIncludeFileLoader(ITextFileLoader includeFileLoader) {
    m_includeFileLoader = includeFileLoader;
  }

  public void setShowLineNumbers(boolean showLineNumbers) {
    m_showLineNumbers = showLineNumbers;
  }

  public String process() throws IOException {
    if ("js".equals(m_inputType)) {
      return processJavascript();
    }
    if ("css".equals(m_inputType)) {
      return processCss();
    }
    System.err.println("Unexpected inputType: " + m_inputType);
    return m_input;
  }

  protected String processJavascript() throws IOException {
    String content = m_input;
    content = replaceIncludeDirectives(content);
    content = replaceConstants(content);
    return content;
  }

  protected String processCss() throws IOException {
    String content = m_input;
    content = replaceIncludeDirectives(content);
    content = processCssWithLess(content);
    return content;
  }

  private String processCssWithLess(String content) {
    LessEngine engine = new LessEngine();
    try {
      return engine.compile(content);
    }
    catch (LessException e) {
      System.err.println("Failed to parse CSS content with LESS");
      e.printStackTrace(System.err);
      return content;
    }
  }

  protected String replaceIncludeDirectives(String content) throws IOException {
    ITextFileLoader loader = m_includeFileLoader;
    if (m_showLineNumbers) {
      loader = new ITextFileLoader() {
        @Override
        public String read(String path) throws IOException {
          String text = m_includeFileLoader.read(path);
          String name = path;
          int i = name.lastIndexOf('/');
          if (i >= 0) {
            name = name.substring(i + 1);
          }
          i = name.lastIndexOf('.');
          if (i >= 0) {
            name = name.substring(0, i);
          }
          int lineNo = 1;
          boolean insideBlockComment = false;
          StringBuilder buf = new StringBuilder();
          String[] lines = text.split("[\\n]");
          for (String line : lines) {
            buf.append((insideBlockComment ? "//" : "/*")).
            append(name).append(":").
            append(String.format("%-" + ((lines.length + "").length()) + "d", lineNo)).
            append((insideBlockComment ? "//" : "*/")).append(" ").
            append(line).
            append("\n");
            if (lineIsBeginOfMultilineBlockComment(line, insideBlockComment)) {
              //also if line is endMLBC AND beginMLBC
              insideBlockComment = true;
            }
            else if (lineIsEndOfMultilineBlockComment(line, insideBlockComment)) {
              insideBlockComment = false;
            }
            lineNo++;
          }
          return buf.toString();
        }
      };
    }
    return TextFileUtil.processIncludeDirectives(content, loader);
  }

  protected String replaceConstants(String content) throws IOException {
    //TODO imo
    return content;
  }

  protected boolean lineIsBeginOfMultilineBlockComment(String line, boolean insideBlockComment) {
    int a = line.lastIndexOf("/*");
    int b = line.lastIndexOf("*/");
    int c = line.lastIndexOf("/*/");
    return a >= 0 && (b < 0 || b < a || (c == a)) && !insideBlockComment;
  }

  protected boolean lineIsEndOfMultilineBlockComment(String line, boolean insideBlockComment) {
    int a = line.indexOf("/*");
    int b = line.indexOf("*/");
    int c = line.lastIndexOf("/*/");
    return b >= 0 && (a < 0 || a < b || (c == a)) && insideBlockComment;
  }
}
