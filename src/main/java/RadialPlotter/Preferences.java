package RadialPlotter;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class Preferences {

  public Preferences(boolean densityplotter) throws Exception {
    this.densityplotter = densityplotter;
    this.fname = (System.getProperty("user.dir") + "/.");
    fname += densityplotter ? "densityplotter" : "radialplotter";
    this.file = new File(this.fname);
    this.preferences = new HashMap<String,String>();
    read();
  }

  public void delete() throws Exception {
    this.file.delete();
  }

    private void read() throws Exception {
        String aLine;
        try { // read old preferences file
            FileInputStream fin = new FileInputStream(fname);
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));
            int numtokens;
            for (int i=0; (aLine = br.readLine()) != null ; i++) {
                StringTokenizer st = new StringTokenizer(aLine, ": ");
                numtokens = st.countTokens();
                if (numtokens == 2){
                    this.put(st.nextToken(), st.nextToken());
                }
            }
            br.close();
            if (!this.getversion().equals(Main.VERSION)){
                this.defaults();
            }
        } catch (Exception e){
            this.defaults();
        }
    }

    public void write() throws Exception {
        String nl = System.getProperties().getProperty("line.separator");
        BufferedWriter out = new BufferedWriter(new FileWriter(fname));
        Iterator<Map.Entry<String, String>> it = preferences.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            out.write(entry.getKey() + ": " + entry.getValue() + nl);
        }
        out.close();
    }

    public void defaults() throws Exception{
        if (densityplotter){
            this.input("other"); // options: fissiontracks or other
            this.output("densityplot"); // options: radialplot or densityplot
            this.transformation("linear"); // options: linear or logarithmic
        } else {
            this.input("fissiontracks");
            this.output("radialplot");
            this.transformation("logarithmic"); // options: linear, logarithmic or arcsin
        }
        this.setLabels();
        this.sigmalines(false);
        this.datalabels(false);
        this.doKDEfill(true);
        this.doKDEstroke(true);
        this.doPDPfill(false);
        this.doPDPstroke(false);
        this.dohistfill(false);
        this.dohiststroke(true);
        this.dopointsfill(false);
        this.dopointstroke(true);
        this.dotickstroke(false);
        this.dobellstroke(false);
        this.saveprefs(false);
        this.adaptive(true);
        this.epanechnikov(false);
        this.normalise(false);
        this.abanico(false);
        this.bellheight(Preferences.BELLHEIGHT);
        this.KDEfillcolour(hexstring(Preferences.KDEAREACOLOUR));
        this.KDEstrokecolour(hexstring(Preferences.KDELINECOLOUR));
        this.PDPfillcolour(hexstring(Preferences.PDPAREACOLOUR));
        this.PDPstrokecolour(hexstring(Preferences.PDPLINECOLOUR));
        this.histfillcolour(hexstring(Preferences.HISTAREACOLOUR));
        this.histstrokecolour(hexstring(Preferences.HISTLINECOLOUR));
        this.pointsfillcolour(hexstring(Preferences.POINTSAREACOLOUR));
        this.pointstrokecolour(hexstring(Preferences.POINTSLINECOLOUR));
        this.minbarcolour(hexstring(Preferences.MINBARCOLOUR));
        this.maxbarcolour(hexstring(Preferences.MAXBARCOLOUR));
        this.setversion(Main.VERSION);
        this.write();
    }

  public String input() throws Exception {
    return get("input");
  }

  public boolean fissiontracks() throws Exception {
    return get("input").equals("fissiontracks");
  }

  public boolean other() throws Exception {
    return get("input").equals("other");
  }
  
  public void bellheight(int value) throws Exception {
      put("bell_height", String.valueOf(value));
  }
  
  public int bellheight() throws Exception {
      return Integer.parseInt(get("bell_height"));
  }

  public void setLabels() throws Exception {
    if (fissiontracks()){
        this.xlabel("Ns");
        this.ylabel("Ni");
        this.zlabel("[Dpar]");
    } else if (other()) {
        this.xlabel("x");
        this.ylabel("σ(x)");
        this.zlabel("[c]");
    }
  }

  private String hexstring(Color colour) {
    return "0x" + Integer.toHexString(colour.getRGB()).substring(2, 8);
  }

  public void input(String input) throws Exception {
    put("input", input);
  }

  public String output() throws Exception {
    return get("output");
  }

  public boolean densityplot() throws Exception {
    return get("output").equals("densityplot");
  }

  public boolean radialplot() throws Exception {
    return get("output").equals("radialplot");
  }

  public void output(String output) throws Exception {
    put("output", output);
  }
  
  public String getversion() throws Exception {
    return get("version");
  }
  
  public void setversion(String version) throws Exception {
      put("version", version);
  }

  public boolean linear() throws Exception {
    return get("transformation").equals("linear");
  }

  public boolean logarithmic() throws Exception {
    return get("transformation").equals("logarithmic");
  }

  public boolean sqrt() throws Exception {
    return get("transformation").equals("sqrt");
  }
  
  public boolean arcsin() throws Exception {
    return get("transformation").equals("arcsin");
  }

  public String transformation() throws Exception {
    return get("transformation");
  }  
  
  public void transformation(String transformation) throws Exception {
    put("transformation", transformation);
  }

  public String xlabel() throws Exception {
    return get("xlabel");
  }

  public void xlabel(String xlabel) throws Exception {
    put("xlabel", xlabel);
  }

  public String ylabel() throws Exception {
    return get("ylabel");
  }

  public void ylabel(String ylabel) throws Exception {
    put("ylabel", ylabel);
  }

  public String zlabel() throws Exception {
    return get("zlabel");
  }

  public void zlabel(String zlabel) throws Exception {
    put("zlabel", zlabel);
  }

  private void boolput(String key, boolean value) throws Exception {
    String text = value ? "true" : "false";
    put(key, text);
  }

  private boolean boolget(String key) throws Exception {
    return get(key).equals("true");
  }

  public boolean sigmalines() throws Exception {
    return boolget("plot_2-sigma_lines");
  }

  public void sigmalines(boolean sigmalines) throws Exception {
    boolput("plot_2-sigma_lines", sigmalines);
  }

  public boolean datalabels() throws Exception {
    return boolget("plot_data_labels");
  }

  public void datalabels(boolean datalabels) throws Exception {
    boolput("plot_data_labels", datalabels);
  }

  public boolean doKDEfill() throws Exception {
    return boolget("fill_KDE");
  }

  public void doKDEfill(boolean doKDEfill) throws Exception {
    boolput("fill_KDE", doKDEfill);
  }

  public boolean doKDEstroke() throws Exception {
    return boolget("stroke_KDE");
  }

  public void doKDEstroke(boolean doKDEstroke) throws Exception {
    boolput("stroke_KDE", doKDEstroke);
  }

  public boolean doPDPfill() throws Exception {
    return boolget("fill_PDP");
  }

  public void doPDPfill(boolean doPDPfill) throws Exception {
    boolput("fill_PDP", doPDPfill);
  }

  public boolean doPDPstroke() throws Exception {
    return boolget("stroke_PDP");
  }

  public void doPDPstroke(boolean doPDPstroke) throws Exception {
    boolput("stroke_PDP", doPDPstroke);
  }

  public boolean dohistfill() throws Exception {
    return boolget("fill_histogram");
  }

  public void dohistfill(boolean dohistfill) throws Exception {
    boolput("fill_histogram", dohistfill);
  }

  public boolean dohiststroke() throws Exception {
    return boolget("stroke_histogram");
  }

  public void dohiststroke(boolean dohiststroke) throws Exception {
    boolput("stroke_histogram", dohiststroke);
  }

  public boolean dopointsfill() throws Exception {
    return boolget("fill_data_points");
  }

  public void dopointsfill(boolean dopointsfill) throws Exception {
    boolput("fill_data_points", dopointsfill);
  }

  public boolean dopointstroke() throws Exception {
    return boolget("stroke_data_points");
  }

  public void dopointstroke(boolean dopointstroke) throws Exception {
    boolput("stroke_data_points", dopointstroke);
  }

  public void dotickstroke(boolean dotickstroke) throws Exception {
    boolput("stroke_data_ticks", dotickstroke);
  }
  
  public boolean dotickstroke() throws Exception {
      return boolget("stroke_data_ticks");
  }

  public void dobellstroke(boolean dobellstroke) throws Exception {
    boolput("stroke_data_bell",dobellstroke);
  }  
  
  public boolean dobellstroke() throws Exception {
      return boolget("stroke_data_bell");
  }

  public void abanico(boolean abanico) throws Exception {
    boolput("abanico",abanico);
  }  
  
  public boolean abanico() throws Exception {
      return boolget("abanico");
  }
  
  public boolean saveprefs() throws Exception {
    return boolget("save_preferences");
  }

  public void saveprefs(boolean saveprefs) throws Exception {
    boolput("save_preferences", saveprefs);
  }

  public boolean adaptive() throws Exception {
    return boolget("adaptive");
  }

  public void adaptive(boolean adapt) throws Exception {
    boolput("adaptive", adapt);
  }  

  public boolean epanechnikov() throws Exception {
    return boolget("epanechnikov");
  }

  public void epanechnikov(boolean epanechnikov) throws Exception {
    boolput("epanechnikov", epanechnikov);
  }    

  public boolean normalise() throws Exception {
    return boolget("normalise");
  }

  public void normalise(boolean normalise) throws Exception {
    boolput("normalise", normalise);
  }
  
  public Color KDEfillcolour() throws Exception {
    return Color.decode(get("KDE_fill_colour"));
  }

  public void KDEfillcolour(String colour) throws Exception {
    put("KDE_fill_colour", colour);
  }

  public void KDEfillcolour(Color colour) throws Exception {
    KDEfillcolour(hexstring(colour));
  }

  public Color KDEstrokecolour() throws Exception {
    return Color.decode(get("KDE_stroke_colour"));
  }

  public void KDEstrokecolour(String colour) throws Exception {
    put("KDE_stroke_colour", colour);
  }

  public void KDEstrokecolour(Color colour) throws Exception {
    KDEstrokecolour(hexstring(colour));
  }

  public Color PDPfillcolour() throws Exception {
    return Color.decode(get("PDP_fill_colour"));
  }

  public void PDPfillcolour(String colour) throws Exception {
    put("PDP_fill_colour", colour);
  }

  public void PDPfillcolour(Color colour) throws Exception {
    PDPfillcolour(hexstring(colour));
  }

  public Color PDPstrokecolour() throws Exception {
    return Color.decode(get("PDP_stroke_colour"));
  }

  public void PDPstrokecolour(String colour) throws Exception {
    put("PDP_stroke_colour", colour);
  }

  public void PDPstrokecolour(Color colour) throws Exception {
    PDPstrokecolour(hexstring(colour));
  }

  public Color histfillcolour() throws Exception {
    return Color.decode(get("hist_fill_colour"));
  }

  public void histfillcolour(String colour) throws Exception {
    put("hist_fill_colour", colour);
  }

  public void histfillcolour(Color colour) throws Exception {
    histfillcolour(hexstring(colour));
  }

  public Color histstrokecolour() throws Exception {
    return Color.decode(get("hist_stroke_colour"));
  }

  public void histstrokecolour(String colour) throws Exception {
    put("hist_stroke_colour", colour);
  }

  public void histstrokecolour(Color colour) throws Exception {
    histstrokecolour(hexstring(colour));
  }

  public Color pointsfillcolour() throws Exception {
    return Color.decode(get("points_fill_colour"));
  }

  public void pointsfillcolour(String colour) throws Exception {
    put("points_fill_colour", colour);
  }

  public void pointsfillcolour(Color colour) throws Exception {
    pointsfillcolour(hexstring(colour));
  }

  public Color pointstrokecolour() throws Exception {
    return Color.decode(get("points_stroke_colour"));
  }

  public void pointstrokecolour(String colour) throws Exception {
    put("points_stroke_colour", colour);
  }

  public void pointstrokecolour(Color colour) throws Exception {
    pointstrokecolour(hexstring(colour));
  }

  public Color minbarcolour() throws Exception {
    return Color.decode(get("min_bar_colour"));
  }

  public void minbarcolour(String colour) throws Exception {
    put("min_bar_colour", colour);
  }

  public void minbarcolour(Color colour) throws Exception {
    minbarcolour(hexstring(colour));
  }

  public Color maxbarcolour() throws Exception {
    return Color.decode(get("max_bar_colour"));
  }

  public void maxbarcolour(String colour) throws Exception {
    put("max_bar_colour", colour);
  }

  public void maxbarcolour(Color colour) throws Exception {
    maxbarcolour(hexstring(colour));
  }
  
  private String get(String key) throws Exception {
      return this.preferences.get(key);
  }
  
  private void put(String key, String value) throws Exception {
    this.preferences.put(key, value);
    write();
  }
  
    private String fname;
    private final HashMap<String, String> preferences;
    private final boolean densityplotter;
    private final File file;
    static final Color KDEAREACOLOUR = Color.cyan, KDELINECOLOUR=Color.blue,
                PDPAREACOLOUR = Color.magenta, PDPLINECOLOUR=Color.black,
                HISTAREACOLOUR = Color.white, HISTLINECOLOUR = Color.gray,
                POINTSAREACOLOUR = Color.yellow, POINTSLINECOLOUR = Color.black,
                MINBARCOLOUR = Color.yellow, MAXBARCOLOUR = Color.red;
    static final int BELLHEIGHT = 10;

}