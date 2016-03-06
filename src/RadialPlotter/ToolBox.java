package RadialPlotter;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class ToolBox {

      public static boolean isNumeric(String str)
      {
        try
        {
          Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
          return false;
        }
        return true;
      }

    static public double getMax(ArrayList<Double> arraylist) throws Exception {
        double x, max = arraylist.get(0);
        for (int i=0;i<arraylist.size();i++){
            x = arraylist.get(i);
            if (x>max){
                max = x;
            }
        }
        return max;
    }

    static public double log(double num) throws Exception {
        double result = Math.log(num);
        if (Double.isNaN(result)){
            throw new ArithmeticException("Error: log of negative number.");
        }
        return result;
    }

    static public double log10(double num) throws Exception {
        double result = Math.log10(num);
        if (Double.isNaN(result)){
            throw new ArithmeticException("log of negative number");
        }
        return result;
    }

    static public double getMin(ArrayList<Double> arraylist) throws Exception {
        double x, min = arraylist.get(0);
        for (int i=0;i<arraylist.size();i++){
            x = arraylist.get(i);
            if (x<min){
                min = x;
            }
        }
        return min;
    }

    // converts a double t to a string with n significant digits
    static public String num2string(double t, int n) throws Exception {
        DecimalFormat formatter = sigdig(t,n);
        return formatter.format(t);
    }

    static public String[] num2string(double x, double xerr, int n) throws Exception {
        String[] out = new String[2];
        DecimalFormat formatter = sigdig(xerr,n);
        out[0] = formatter.format(x);
        out[1] = formatter.format(xerr);
        return out;
    }

    static DecimalFormat sigdig(double t, int n) throws Exception {
        int numdecimals = (t>0) ?(int) Math.floor(ToolBox.log10(t)) : 0;
        String fmt = (numdecimals < n) ? "#." : "#";
        for (int i=0; i<n-numdecimals; i++){ fmt += "#";}
        return new DecimalFormat(fmt);
    }

    public static String superscript(String str) {
        str = str.replaceAll("0", "⁰");
        str = str.replaceAll("1", "¹");
        str = str.replaceAll("2", "²");
        str = str.replaceAll("3", "³");
        str = str.replaceAll("4", "⁴");
        str = str.replaceAll("5", "⁵");
        str = str.replaceAll("6", "⁶");
        str = str.replaceAll("7", "⁷");
        str = str.replaceAll("8", "⁸");
        str = str.replaceAll("9", "⁹");
        return str;
    }

    public static String subscript(String str) {
        str = str.replaceAll("0", "₀");
        str = str.replaceAll("1", "₁");
        str = str.replaceAll("2", "₂");
        str = str.replaceAll("3", "₃");
        str = str.replaceAll("4", "₄");
        str = str.replaceAll("5", "₅");
        str = str.replaceAll("6", "₆");
        str = str.replaceAll("7", "₇");
        str = str.replaceAll("8", "₈");
        str = str.replaceAll("9", "₉");
        return str;
    }
    
    public static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }    

    static double interp(double x, double[] pdf, double[] timescale) {
        if (x<timescale[0] | x>timescale[timescale.length-1]){
            return Data.NAN;
        }
        for (int i=1; i<timescale.length; i++){
            if (x<timescale[i]) {
                return pdf[i-1]+(pdf[i]-pdf[i-1])*(x-timescale[i-1])/(timescale[i]-timescale[i-1]);
            }
        }
        return Data.NAN;
    }

    static JEditorPane myJEditorPane(String message, final String url, final boolean exit) {
        JEditorPane ep = new JEditorPane("text/html", "<html><body><center>" +
                            message + "</center></body></html>");
        // handle link events
        ep.addHyperlinkListener(new HyperlinkListener(){
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e){
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                openURL(url, exit);
            }
        });
        ep.setEditable(false);   
        return ep;
    }
    
    static void openURL(String url, boolean exit) {
        try {
            if(Desktop.isDesktopSupported()){
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
            if (exit) System.exit(0);
        } catch (URISyntaxException e) {
            if (Data.debugmode){e.printStackTrace(System.out);}
        } catch (IOException e) {
            if (Data.debugmode){e.printStackTrace(System.out);}
        }
    }
    
    public static double[] convertDoubles(List<Double> doubles){
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = doubles.get(i);
        }
        return ret;
    }
    
    public static double round(double in) {
        return (Math.round(in*1e10)/1e10);
    }
    
    public static double getMax(double[] in) throws Exception {
        double[] sorted = in.clone();
        Arrays.sort(sorted);
        return(sorted[in.length-1]);
    }

    static double getmaxabs(double ming, double maxg) {
        double aming = Math.abs(ming);
        double amaxg = Math.abs(maxg);
        if (aming > amaxg) {
            return aming;
        } else {
            return amaxg;
        }
    }
    
}