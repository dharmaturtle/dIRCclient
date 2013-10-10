/**
 *  This is
 *  the fun part.
 */

package client;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import org.apache.commons.lang3.text.WordUtils;

public class IRCGUI extends JPanel implements ActionListener{
  protected JTextField textField;
  protected JTextPane textPane;
  private IRCDriver bot;
  private DateFormat dateFormat;
  private Date date;
  private String username;
  private String password;
  private Element ele;
  private Element first;
  private SimpleAttributeSet mainTextAttributes;
  private SimpleAttributeSet nameAttributes;
  private HashMap<String, String> userColors;
  private ArrayList<String> subs;
  private ArrayList<String> mods;

  public IRCGUI(){
    super(new GridBagLayout());
    initialize();
    this.userColors = new HashMap();
    this.mods = new ArrayList();
    this.subs = new ArrayList();
  }

  public void actionPerformed(ActionEvent evt){
    String text = this.textField.getText();

    if (text.startsWith("/join ")){
      String[] splitText = text.split(" ");
      append("SYSTEM", "JOINING CHANNEL: " + splitText[1]);
      this.textField.setText("");
      joinChannel(splitText[1]);
      return;
    }
    this.bot.sendMessage(text);
    append(this.bot.getName(), text);
    this.textField.setText("");
  }

  public synchronized void append(String userNick, String message){
    try{
      Document doc = this.textPane.getDocument();
      this.mainTextAttributes = new SimpleAttributeSet();
      StyleConstants.setForeground(this.mainTextAttributes, new Color(0.95F, 0.95F, 0.95F)); // I work in percentages. Nonstandard, but I like it.
      StyleConstants.setFontSize(this.mainTextAttributes, 14);
      this.nameAttributes = fetchNameAttributes(userNick);
      this.date = new Date();
      // formats the incoming or outgong text with [timestamp] optionalflag <nick> message
      doc.insertString(doc.getLength(), "[" + this.dateFormat.format(this.date) + "] ", this.mainTextAttributes);
      checkModStatus(userNick);
      checkSubStatus(userNick);
      doc.insertString(doc.getLength(), " <", this.mainTextAttributes);
      doc.insertString(doc.getLength(), WordUtils.capitalize(userNick), this.nameAttributes);
      doc.insertString(doc.getLength(), "> " + message + "\n", this.mainTextAttributes);

      if (doc.getLength() > 5000){ // maximum lines visible before they start disappearing into the ether
        this.ele = doc.getDefaultRootElement();
        this.first = this.ele.getElement(0);
        doc.remove(this.first.getStartOffset(), this.first.getEndOffset());
      }
    }
    catch (BadLocationException exc){
      exc.printStackTrace();
    }
    this.textPane.selectAll();
    this.textPane.setCaretPosition(this.textPane.getSelectionEnd());
  }

  private void initialize(){ // main connect
    this.dateFormat = new SimpleDateFormat("HH:mm");
    this.textField = new JTextField(45);
    this.textField.addActionListener(this);
    this.textPane = new JTextPane();
    this.textPane.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(this.textPane);
    scrollPane.setPreferredSize(new Dimension(800, 600));
    DefaultCaret caret = (DefaultCaret)this.textPane.getCaret();
    caret.setUpdatePolicy(2);
    GridBagConstraints c = new GridBagConstraints();
    c.gridwidth = 0;
    c.fill = 1;
    c.weightx = 1.0D;
    c.weighty = 1.0D;
    add(scrollPane, c);
    c.weightx = 0.0D;
    c.weighty = 0.0D;
    c.fill = 2;
    add(this.textField, c);
    this.textPane.setBackground(new Color(0.05F, 0.05F, 0.05F));
    Document doc = this.textPane.getDocument();
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    StyleConstants.setForeground(attributes, new Color(0.9F, 0.9F, 0.9F));
    StyleConstants.setFontSize(attributes, 14);
    this.date = new Date();
    try{
      doc.insertString(doc.getLength(), "dIRC Twitch Client v2.0\n", attributes);
    }
    catch (BadLocationException e){
      e.printStackTrace();
    }
    requestCredentials();
    String channel = JOptionPane.showInputDialog("Which stream are we connecting to?").toLowerCase();
    IRCDriver irc = new IRCDriver(channel, this, this.username, this.password);
    irc.connect();
    this.bot = irc;
  }

  public void exit(){
    this.bot.disconnect();
  }

  public void requestCredentials(){ // initial screen
    JFrame guiFrame = new JFrame();
    guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    guiFrame.setTitle("Sign In");
    guiFrame.setSize(500, 300);
    guiFrame.setLocationRelativeTo(null);
    guiFrame.setVisible(false);
    JTextArea tracker = new JTextArea("Password Tracker:");
    guiFrame.add(tracker);
    JPanel userPanel = new JPanel();
    userPanel.setLayout(new GridLayout(2, 2));
    JLabel usernameLbl  = new JLabel("Username:");
    JLabel passwordLbl  = new JLabel("Password:");
    JTextField username = new JTextField();
    JPasswordField passwordFld = new JPasswordField();

    userPanel.add(usernameLbl);
    userPanel.add(username);
    userPanel.add(passwordLbl);
    userPanel.add(passwordFld);

    int input = JOptionPane.showConfirmDialog(guiFrame, userPanel, "Login Screen", 2, -1);

    if (input == 0){
      this.username = username.getText();
      char[] enteredPassword = passwordFld.getPassword();
      this.password = String.valueOf(enteredPassword);
      Arrays.fill(enteredPassword, '0');
      guiFrame.removeAll();
      guiFrame.dispose();
    }
    else{
      tracker.append("\nDialog cancelled..");
    }
  }

  public void joinChannel(String channel){ // restarts everything
    this.mods.clear();
    this.subs.clear();
    this.bot.disconnect();
    try {
      Thread.sleep(250L);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    this.bot = null;
    this.bot = new IRCDriver(channel.toLowerCase(), this, this.username, this.password);
    this.bot.connect();
  }

  public void addUserColor(String username, String color){
    if (!this.userColors.containsKey(username.toLowerCase())){
      this.userColors.put(username.toLowerCase(), color);
    }
  }

  public void addMod(String userName){
    if (!this.mods.contains(userName)){
      this.mods.add(userName);
    }
  }

  public void addSubscriber(String userName){
    userName = userName.toLowerCase();
    if (!this.subs.contains(userName)){
      this.subs.add(userName);
    }
  }

  private void checkModStatus(String userName){
    if (this.mods.contains(userName.toLowerCase())){
      try{
        InputStream input = getClass().getResourceAsStream("modicon.png");
        Image temp = ImageIO.read(input);
        BufferedImage img = (BufferedImage)temp;
        ImageIcon pictureImage = new ImageIcon(img);
        this.textPane.insertIcon(pictureImage);
      }
      catch (IOException localIOException){
      }
    }
  }

  private void checkSubStatus(String userName){
    userName = userName.toLowerCase();
    if (this.subs.contains(userName)){
      SimpleAttributeSet bracketStyle = new SimpleAttributeSet();
      StyleConstants.setForeground(bracketStyle, new Color(0.95F, 0.95F, 0.95F));
      StyleConstants.setFontSize(bracketStyle, 14);
      SimpleAttributeSet tagStyle = new SimpleAttributeSet();
      StyleConstants.setForeground(tagStyle, new Color(1.0F, 0.9F, 0.0F));
      StyleConstants.setFontSize(tagStyle, 14);
      Document doc = this.textPane.getDocument();
      try{
        doc.insertString(doc.getLength(), " [", bracketStyle); //subscriber flag
        doc.insertString(doc.getLength(), "SUB", tagStyle);
        doc.insertString(doc.getLength(), "] ", bracketStyle);
      }
      catch (BadLocationException e){
        e.printStackTrace();
      }
    }
  }

  private SimpleAttributeSet fetchNameAttributes(String userName){
    SimpleAttributeSet temp = new SimpleAttributeSet();
    StyleConstants.setFontSize(temp, 14);
    if (this.userColors.containsKey(userName.toLowerCase())){
      String cString = (String)this.userColors.get(userName.toLowerCase());
      StyleConstants.setForeground(temp, new Color(Integer.parseInt(cString.substring(1, 3), 16), Integer.parseInt(cString.substring(3, 5), 16), Integer.parseInt(cString.substring(5, 7), 16))); // goes in order of red, green, blue
    }
    else{
      StyleConstants.setForeground(temp, new Color(0.7F, 0.7F, 0.7F));
    }
    return temp;
  }
}