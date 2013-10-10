/**
 *  Event Listener
 *  Listens to messages from the server, pretty easy
 *  Handles subs, mods, disconnects, messages, the whole shebang
 *  I miss Python
 */

package client;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

public class ClientMessageListener extends ListenerAdapter{
  private String message;
  private String userNick;
  private String privateMessage;
  private String privateUserNick;
  private IRCGUI gui;
  public ClientMessageListener(IRCGUI gui){
    this.gui = gui;
  }

  public void onMessage(MessageEvent m){
    this.message = m.getMessage();
    this.userNick = m.getUser().getNick();
    this.gui.append(this.userNick, this.message);
  }

  public void onPrivateMessage(PrivateMessageEvent pm){
    this.privateMessage = pm.getMessage();
    this.privateUserNick = pm.getUser().getNick();
    if ((this.privateUserNick.equals("jtv")) && (this.privateMessage.contains("USERCOLOR"))) storeColor();
    if ((this.privateMessage.contains("subscriber")) && (this.privateUserNick.equals("jtv"))) addSubscriber();
  }

  public void onDisconnect(DisconnectEvent d){
    this.gui = null;
    this.message = null;
    this.userNick = null;
  }

  public void onMode(ModeEvent m){
    if ((m.getUser().getNick().equals("jtv")) && (m.getMode().contains("+o"))) addModerator(m);
  }

  private void addSubscriber(){
    this.gui.addSubscriber(this.privateMessage.split(" ")[1]); // name of sub
  }

  private void addModerator(ModeEvent m){
    this.gui.addMod(m.getMode().split(" ")[1]);
  }

  private void storeColor(){
    String[] messageSplit = this.privateMessage.split(" ");
    this.gui.addUserColor(messageSplit[1], messageSplit[2]); // 1 is user, 2 is color
  }

  public void channelJoin(){
    this.gui.joinChannel(this.message.split(" ")[1]);
  }
}