/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.mailfield;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.MailUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.mailfield.IMailField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;
import org.eclipse.scout.rt.ui.swing.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutMailField extends SwingScoutValueFieldComposite<IMailField> implements ISwingScoutMailField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutMailField.class);
  private static final String KEY_FROM = "From";
  private static final String KEY_TO = "To";
  private static final String KEY_CC = "Cc";
  private static final String KEY_BCC = "bcc";

  private Map<String, P_AddressComponent> m_addressComponents;

  private HTMLEditorKit m_htmlKit;
  private HTMLDocument m_htmlDoc;
  private StyleSheet m_styleSheet;
  private JTextPane m_htmlView;
  private JPanelEx m_htmlViewPanel;
  private JScrollPane m_scrollPane;
  private ArrayList<SwingMailAttachment> m_attachments;
  private JPanel m_attachementPanel;
  private MouseListener m_attachementListener;
  private File m_tempFolder;
  private JLabelEx m_sentLabel;
  private JLabelEx m_receivedDate;
  private JLabelEx m_subjectLabel;
  private JLabelEx m_subject;

  @Override
  protected void detachScout() {
    if (m_tempFolder != null) {
      IOUtility.deleteDirectory(m_tempFolder);
    }
    super.detachScout();
  }

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    m_attachementListener = new P_AttachementMouseListener();
    if (m_addressComponents == null) {
      m_addressComponents = new HashMap<String, P_AddressComponent>();
    }
    m_attachments = new ArrayList<SwingMailAttachment>();
    JPanel container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    JComponent mailPanel = createMailComponent();
    mailPanel.putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, LogicalGridDataBuilder.createField(getSwingEnvironment(), getScoutObject().getGridData()));
    container.add(mailPanel);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(m_htmlView);
    // set initial size to text field (for layout algorithm
    // "what is height, given with=x")
    // m_htmlView.setSize(m_htmlViewPanel.getPreferredSize());
    //
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected JComponent createMailComponent() {
    JPanelEx mailContainer = new JPanelEx(new BorderLayoutEx(0, 0));
    JComponent header = createHeaderComponent();
    JComponent body = createBodyComponent();
    JComponent attachements = createAttachementComponent();

    JPanelEx bodyAttachmentPanel = new JPanelEx(new BorderLayoutEx(0, 0));
    bodyAttachmentPanel.setBorder(BorderFactory.createEtchedBorder());
    bodyAttachmentPanel.add(body, BorderLayoutEx.CENTER);
    bodyAttachmentPanel.add(attachements, BorderLayoutEx.SOUTH);

    mailContainer.add(header, BorderLayoutEx.NORTH);
    mailContainer.add(bodyAttachmentPanel, BorderLayoutEx.CENTER);
    return mailContainer;
  }

  protected JComponent createBodyComponent() {
    // JPanel bodyPanel = new JPanel();
    // viewer
    m_htmlKit = new HTMLEditorKit();
    m_htmlDoc = (HTMLDocument) (m_htmlKit.createDefaultDocument());
    m_styleSheet = m_htmlDoc.getStyleSheet();
    //
    m_htmlView = new JTextPaneEx();
    m_htmlView.setEditorKit(m_htmlKit);
    m_htmlView.setDocument(m_htmlDoc);
    m_htmlView.setEditable(false);
    m_htmlView.setBorder(null);
    m_htmlView.setMargin(null);
    m_htmlView.setCaretPosition(0);
    m_htmlView.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
          fireHyperlinkActionFromSwing(e.getURL());
        }
      }
    });
    //
    m_htmlViewPanel = new JPanelEx(new SingleLayout());
    if (getScoutObject().isScrollBarEnabled()) {
      m_scrollPane = new JScrollPaneEx(m_htmlView);
      m_htmlViewPanel.add(m_scrollPane);
    }
    else {
      m_htmlViewPanel.add(m_htmlView);
    }

    return m_htmlViewPanel;
  }

  protected JComponent createHeaderComponent() {
    JPanel headerPanel = new JPanel(new GridBagLayout());
    // FROM
    P_AddressComponent fromComp = new P_AddressComponent();
    m_addressComponents.put(KEY_FROM, fromComp);
    // TO
    P_AddressComponent toComp = new P_AddressComponent();
    m_addressComponents.put(KEY_TO, toComp);
    // CC
    P_AddressComponent ccComp = new P_AddressComponent();
    m_addressComponents.put(KEY_CC, ccComp);
    // SENT
    m_sentLabel = new JLabelEx();
    m_receivedDate = new JLabelEx();
    // SUBJECT
    m_subjectLabel = new JLabelEx();
    m_subject = new JLabelEx();

    // layout
    GridBagConstraints constrains = new GridBagConstraints();
    constrains.gridx = 0;
    constrains.gridy = 0;
    constrains.weightx = 0.5;
    constrains.insets = new Insets(0, 0, 0, 0);
    constrains.fill = GridBagConstraints.BOTH;
    headerPanel.add(fromComp, constrains);

    constrains.gridx = 0;
    constrains.gridy = 1;
    headerPanel.add(toComp, constrains);

    constrains.gridx = 0;
    constrains.gridy = 2;
    headerPanel.add(ccComp, constrains);

    constrains.gridx = 0;
    constrains.gridy = 3;
    headerPanel.add(createReceivedDatePanel(), constrains);

    constrains.gridx = 0;
    constrains.gridy = 4;
    headerPanel.add(createSubjectPanel(), constrains);

    return headerPanel;
  }

  private Component createSubjectPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints constrains = new GridBagConstraints();
    constrains.insets = new Insets(2, 2, 2, 2);
    constrains.gridx = 0;
    constrains.gridy = 0;
    constrains.weightx = 0;
    m_subjectLabel.setMinimumSize(new Dimension(80, 20));
    m_subjectLabel.setPreferredSize(new Dimension(80, 20));
    panel.add(m_subjectLabel, constrains);

    constrains.gridx = 1;
    constrains.gridy = 0;
    constrains.weightx = 0.5;
    constrains.fill = GridBagConstraints.BOTH;
    panel.add(m_subject, constrains);

    return panel;
  }

  private Component createReceivedDatePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints Constrains = new GridBagConstraints();
    Constrains.insets = new Insets(2, 2, 2, 2);
    Constrains.gridx = 0;
    Constrains.gridy = 0;
    Constrains.weightx = 0;
    m_sentLabel.setMinimumSize(new Dimension(80, 20));
    m_sentLabel.setPreferredSize(new Dimension(80, 20));
    panel.add(m_sentLabel, Constrains);

    Constrains.gridx = 1;
    Constrains.gridy = 0;
    Constrains.weightx = 0.5;
    Constrains.fill = GridBagConstraints.BOTH;
    panel.add(m_receivedDate, Constrains);

    return panel;
  }

  protected JComponent createAttachementComponent() {
    //scrollable when more than one line of attachements
    m_attachementPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.LEFT));
    JScrollPaneEx pane = new JScrollPaneEx(m_attachementPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    return pane;
  }

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    updateFromLabelFromScout();
    updateToLabelFromScout();
    updateCcLabelFromScout();
    updateSentLabelFromScout();
    updateSubjectLabelFromScout();
  }

  @Override
  protected void setValueFromScout(Object o) {
    super.setValueFromScout(o);
    setMessageFromScout(getScoutObject().getValue());
  }

  protected void setMessageFromScout(MimeMessage message) {
    String subject = "";
    String receivedDate = "";
    List<Part> bodyCollector = new ArrayList<Part>();
    List<Part> attachementCollector = new ArrayList<Part>();
    Address[] fromAddresses = new Address[0];
    Address[] toAddresses = new Address[0];
    Address[] ccAddresses = new Address[0];
    try {
      if (message != null) {
        MailUtility.collectMailParts(message, bodyCollector, attachementCollector);
        subject = message.getSubject();
        Date received = message.getSentDate();
        if (received != null) {
          receivedDate = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT).format(received);
        }
        // addresses
        fromAddresses = message.getFrom();
        toAddresses = message.getRecipients(Message.RecipientType.TO);
        ccAddresses = message.getRecipients(Message.RecipientType.CC);
      }
    }
    catch (Exception e) {
      LOG.warn("could not parse message.", e);
    }
    setAttachements(attachementCollector.toArray(new Part[attachementCollector.size()]));
    try {
      setBodyParts(bodyCollector.toArray(new Part[bodyCollector.size()]));
    }
    catch (Exception e) {
      LOG.warn("could not parse message.", e);
    }
    m_receivedDate.setText(receivedDate);
    m_subject.setText(subject);
    setAddressesFromScout(fromAddresses, KEY_FROM, false);
    setAddressesFromScout(toAddresses, KEY_TO, false);
    setAddressesFromScout(ccAddresses, KEY_CC, true);
    // setAddressesFromScout(bccAddresses, KEY_BCC, true);
  }

  protected void setAttachements(Part[] attachements) {
    for (Component c : m_attachementPanel.getComponents()) {
      c.removeMouseListener(m_attachementListener);
    }
    m_attachementPanel.removeAll();
    m_attachments.clear();
    if (attachements != null && attachements.length > 0) {
      m_attachementPanel.setVisible(true);
      for (Part p : attachements) {
        SwingMailAttachment att = new SwingMailAttachment(p, getTempFolder());
        m_attachments.add(att);
        if (att.getContentId() == null) {
          // cid is normally used for inline images in html
          SwingMailAttachmentView attView = new SwingMailAttachmentView(att);
          attView.addMouseListener(m_attachementListener);
          m_attachementPanel.add(attView);
        }
      }
    }
    else {
      m_attachementPanel.setVisible(false);
    }
  }

  protected void setBodyParts(Part[] bodyParts) throws MessagingException, ProcessingException, IOException {
    StringBuilder buf = new StringBuilder();
    if (bodyParts != null) {
      Part bodyPart = MailUtility.getHtmlPart(bodyParts);
      if (bodyPart == null) {
        bodyPart = MailUtility.getPlainTextPart(bodyParts);
        if (bodyPart != null) {
          buf.append("<html><body><pre>");
          buf.append((String) bodyPart.getContent());
          buf.append("</pre></body></html>");
        }
      }
      else if (bodyPart instanceof MimePart) {
        MimePart mimePart = (MimePart) bodyPart;
        // encoding
        Pattern pattern = Pattern.compile("charset=\".*\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mimePart.getContentType());
        String htmlCharacterEncoding = "UTF-8"; // default, a good guess in Europe
        if (matcher.find()) {
          if (matcher.group(0).split("\"").length >= 2) {
            htmlCharacterEncoding = matcher.group(0).split("\"")[1];
          }
        }

        byte[] content = IOUtility.getContent(mimePart.getInputStream());
        if (content != null) {
          buf.append(new String(content, htmlCharacterEncoding));
        }
      }
    }
    Font f = UIManager.getFont("Label.font");
    if (f == null) {
      f = new JLabel().getFont();
    }
    HTMLDocument doc = HTMLUtility.cleanupDocument(HTMLUtility.parseDocument(buf.toString()), f.getFamily(), f.getSize());
    HTMLUtility.formatDocument(doc);
    HashMap<String, URL> cidMap = new HashMap<String, URL>();
    for (SwingMailAttachment a : m_attachments) {
      String cid = a.getContentId();
      if (cid != null) {
        cidMap.put(cid, a.getFile().toURI().toURL());
      }
    }
    doc = HTMLUtility.replaceContendIDs(doc, cidMap);
    String resolvedHtml = HTMLUtility.formatDocument(doc);
    m_htmlDoc = (HTMLDocument) (m_htmlKit.createDefaultDocument());
    m_styleSheet = m_htmlDoc.getStyleSheet();
    m_htmlView.setDocument(m_htmlDoc);
    m_htmlView.setText(resolvedHtml);
    m_htmlView.setCaretPosition(0);
  }

  public JTextPane getSwingMailField() {
    return m_htmlView;
  }

  protected JScrollPane getScrollPane() {
    return m_scrollPane;
  }

  protected HTMLEditorKit getHtmlKit() {
    return m_htmlKit;
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // super.setEnabledFromScout(b);
    m_htmlView.setEditable(getScoutObject().isMailEditor());
  }

  protected void setAddressesFromScout(Address[] addresses, String addressKey, boolean invisibleWhenEmpty) {
    P_AddressComponent comp = m_addressComponents.get(addressKey);
    if (comp != null) {
      comp.setAddresses(addresses, invisibleWhenEmpty);
    }
  }

  private void updateSentLabelFromScout() {
    m_sentLabel.setText(getScoutObject().getLabelSent());
  }

  private void updateSubjectLabelFromScout() {
    m_subjectLabel.setText(getScoutObject().getLabelSubject());
  }

  private void updateCcLabelFromScout() {
    m_addressComponents.get(KEY_CC).getLabel().setText(getScoutObject().getLabelCc());
  }

  private void updateToLabelFromScout() {
    m_addressComponents.get(KEY_TO).getLabel().setText(getScoutObject().getLabelTo());

  }

  private void updateFromLabelFromScout() {
    m_addressComponents.get(KEY_FROM).getLabel().setText(getScoutObject().getLabelFrom());

  }

  protected void fireHyperlinkActionFromSwing(final URL url) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireHyperlinkActionFromUI(url);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  protected void fireAttachementActionFromSwing(final File file) {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireAttachementActionFromUI(file);
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 2345);
  }

  protected void handleSwingPopup(final SwingMailAttachmentView target) {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        IMenu[] scoutMenus = new IMenu[]{new P_AttachmentPopupMenu(target.getAttachment().getFile())};
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), target, new Point(0, target.getHeight()), scoutMenus).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
  }

  protected File getTempFolder() {
    if (m_tempFolder == null) {
      File folder = null;
      try {
        folder = File.createTempFile("attachements." + hashCode(), "");
        folder.delete();
        folder.mkdir();
        folder.deleteOnExit();
      }
      catch (IOException e) {
        LOG.error("could not create temp directory for mail attachement.", e);
      }
      m_tempFolder = folder;
    }
    return m_tempFolder;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IMailField.PROP_LABEL_FROM.equals(name)) {
      updateFromLabelFromScout();
    }
    else if (IMailField.PROP_LABEL_TO.equals(name)) {
      updateToLabelFromScout();
    }
    else if (IMailField.PROP_LABEL_CC.equals(name)) {
      updateCcLabelFromScout();
    }
    else if (IMailField.PROP_LABEL_SENT.equals(name)) {
      updateSentLabelFromScout();
    }
    else if (IMailField.PROP_LABEL_SUBJECT.equals(name)) {
      updateSubjectLabelFromScout();
    }
    super.handleScoutPropertyChange(name, newValue);
  }

  private class P_AttachementMouseListener extends MouseAdapter {
    MouseClickedBugFix fix;

    @Override
    public void mousePressed(MouseEvent e) {
      fix = new MouseClickedBugFix(e);
      if (e.isPopupTrigger()) {
        Object source = e.getSource();
        if (source instanceof SwingMailAttachmentView) {
          handleSwingPopup(((SwingMailAttachmentView) source));
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        Object source = e.getSource();
        if (source instanceof SwingMailAttachmentView) {
          handleSwingPopup(((SwingMailAttachmentView) source));
        }
      }
      fix.mouseReleased(this, e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (fix.mouseClicked()) return;
      Object source = e.getSource();
      if (source instanceof SwingMailAttachmentView) {
        final File file = ((SwingMailAttachmentView) source).getAttachment().getFile();
        switch (e.getButton()) {
          case MouseEvent.BUTTON1:
            fireAttachementActionFromSwing(file);
            break;

          default:
            break;
        }

      }
    }
  }

  private class P_AddressComponent extends JPanel {
    private static final long serialVersionUID = 1L;
    private JLabelEx m_addressField;
    private InternetAddress[] m_addresses;
    private JLabelEx m_label;

    public P_AddressComponent() {
      super(new GridBagLayout());
      createComponent();
    }

    void createComponent() {
      m_label = new JLabelEx();
      m_label.setMinimumSize(new Dimension(80, 20));
      m_label.setPreferredSize(new Dimension(80, 20));
      m_addressField = new JLabelEx();
      // m_addressField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      // JScrollPane scrollPane=new JScrollPaneEx(m_addressField);
      // layout
      GridBagConstraints constrains = new GridBagConstraints();
      constrains.insets = new Insets(2, 2, 2, 2);
      constrains.gridx = 0;
      constrains.gridy = 0;
      constrains.weightx = 0;
      add(m_label, constrains);

      constrains.gridx = 1;
      constrains.gridy = 0;
      constrains.weightx = 0.5;
      constrains.fill = GridBagConstraints.BOTH;
      add(m_addressField, constrains);
    }

    public JLabelEx getLabel() {
      return m_label;
    }

    void setAddresses(Address[] addresses, boolean invisibleWhenEmpty) {
      ArrayList<InternetAddress> inetAddresses = new ArrayList<InternetAddress>();
      if (addresses != null) {
        for (Address a : addresses) {
          if (a instanceof InternetAddress) {
            inetAddresses.add((InternetAddress) a);
          }
        }
      }
      m_addresses = inetAddresses.toArray(new InternetAddress[inetAddresses.size()]);
      StringBuffer buf = new StringBuffer();
      boolean hasAddresses = m_addresses != null && m_addresses.length > 0;
      if (invisibleWhenEmpty) {
        setVisible(hasAddresses);
      }
      if (hasAddresses) {
        for (InternetAddress address : m_addresses) {
          if (buf.length() > 0) {
            buf.append("; ");
          }
          String adString = address.getPersonal();
          if (adString != null) {
            adString += "<" + address.getAddress() + ">";
          }
          else {
            adString = address.getAddress();
          }
          buf.append(adString);
        }
      }
      m_addressField.setText(buf.toString());
      getParent().doLayout();
    }

  }// end class P_AddressComponent

  private class P_AttachmentPopupMenu extends AbstractMenu {
    private File m_file;

    public P_AttachmentPopupMenu(File file) {
      m_file = file;
    }

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("FormStateStoreAs");
    }

    @Override
    public void doAction() throws ProcessingException {
      String[] extensions = new String[0];
      try {
        String fileName = m_file.getName();
        String fileExt = m_file.getName().substring(fileName.lastIndexOf(".") + 1, fileName.length());
        extensions = new String[]{fileExt};
      }
      catch (Exception e) {
        LOG.warn("could not find extension of '" + m_file.getName() + "'");
      }
      File path = null;
      File dir = null;
      FileChooser fileChooser = new FileChooser(dir, extensions, false);
      fileChooser.setFileName(m_file.getName());
      File[] a = fileChooser.startChooser();
      if (a.length > 0) {
        path = a[0];
        try {
          IOUtility.writeContent(new FileOutputStream(path), IOUtility.getContent(new FileInputStream(m_file)));
        }
        catch (Exception e) {
          LOG.warn(null, e);
        }
      }
    }
  }

}
