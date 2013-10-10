/**
 *  dIRC Twitch Client v2.0
 *  
 *  A lovely Twitch-IRC-Java client
 *  When you type in your password, you *MUST* use the OAuth token you'll find here:
 *  http://twitchapps.com/tmi/
 *
 *  No fancy features yet, just a basic chat interface.
 * 
 */

package client;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class ClientDriver{
  private static IRCGUI gui;
  public static void main(String[] args){
    JFrame frame = new JFrame("dIRC Twitch Client v2.0");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter(){
      
      // exit
      public void windowClosing(WindowEvent e){
        ClientDriver.gui.exit();
        try {
          Thread.sleep(250L);
        }
        catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    });
    gui = new IRCGUI();
    frame.add(gui);
    frame.pack();
    frame.setVisible(true);
  }
}