/**
 *  Simple driver, handles connecting and sending
 *  Sends JTVCLIENT to get access to extra input from Twitch
 *  It says "password" but it really requires an OAuth token
 *  Only one channel supported for now
 */

package client;
import java.io.IOException;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.managers.ListenerManager;

public class IRCDriver extends PircBotX{
  private String channel;
  private String password;
  public IRCDriver(String channel, IRCGUI gui, String username, String password){
    this.channel = channel;
    this.password = password;
    getListenerManager().addListener(new ClientMessageListener(gui));
    setName(username);
  }

  public void connect(){
    setVerbose(true);
    try{
      connect("irc.twitch.tv", 6667, this.password);
      this.password = ""; // clear the password for security
    }
    catch (NickAlreadyInUseException e){
      e.printStackTrace();
    }
    catch (IOException e){
      e.printStackTrace();
    }
    catch (IrcException e){
      e.printStackTrace();
    }
    sendRawLine("JTVCLIENT"); // pretend to be webclient
    joinChannel("#" + this.channel);
  }

  public void sendMessage(String m){
    sendMessage("#" + this.channel, m);
  }
}