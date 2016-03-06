/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package RadialPlotter;

import java.io.InputStream;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

/**
 *
 * @author pvermees
 */
public class Updater {
    
    public static void run(Preferences prefs) {
        try {
            String newversion = getLatestVersion();
            if (!newversion.equals(Main.VERSION)) {             
                showUpdateMessage(newversion, prefs);
            }
        } catch (Exception e) {
            if (Data.DEBUGMODE) {e.printStackTrace(System.out);}
        }
    }
    
    static String getURL() {
        if (Main.DENSITYPLOTTER){
            return "http://densityplotter.london-geochron.com";
        } else {
            return "http://radialplotter.london-geochron.com";
        }
    }
    
    static String getProgramName(String version) {
        String name = (Main.DENSITYPLOTTER) ? "DensityPlotter " : "RadialPlotter ";
        return name + version;
    }
    
    private static void showUpdateMessage(String version, Preferences prefs) throws Exception {
        final String url = getURL(),
                     message = "<br>A new version is available. Would you like<br><br>" + 
                               "to download " + getProgramName(version) + 
                               " from <br><br><a href=\"\">" + url + "</a>?<br>";
        JEditorPane ep = ToolBox.myJEditorPane(message,url,true);
        int reply = JOptionPane.showOptionDialog(null, ep, "update available",  JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,null,null,null);        
        if (reply==0) { ToolBox.openURL(url,true); }
    }
    
    public static String getLatestVersion() throws Exception {
        String data = getData(VERSIONURL),
               label = Main.DENSITYPLOTTER ? "densityplotter" : "radialplotter",
               open = "[" + label + "]",
               close = "[/" + label + "]";
        int start = open.length();
        return data.substring(data.indexOf(open)+start,data.indexOf(close));
    }

    private static String getData(String address)throws Exception {
        URL url = new URL(address);        
        InputStream html;
        html = url.openStream();
        int c = 0;
        StringBuilder buffer = new StringBuilder("");
        while(c != -1) {
            c = html.read();            
            buffer.append((char)c);
        }
        return buffer.toString();
    }
    
    static final String VERSIONURL = "http://ucl.ac.uk/~ucfbpve/software/version.html";
    
}
