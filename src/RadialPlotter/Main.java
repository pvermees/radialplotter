package RadialPlotter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.freehep.graphicsbase.util.UserProperties;
import org.freehep.graphicsio.pdf.PDFGraphics2D;

public class Main extends javax.swing.JFrame {

    /** Creates new form Main
     * @param args */
    public Main(String[] args) {        
        super("Input");
        setLookAndFeel();
        initComponents();
        myInit();
        plotframe = new PlotFrame(data);
        addFrameActions();
        clipboard = new ExcelAdapter(DataTable);
        DataTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        DataTable.setCellSelectionEnabled(true);
        table = (DefaultTableModel)DataTable.getModel();
        lineTable = new LineNumberTable(DataTable);
        scrollpane.setRowHeaderView(lineTable);
        this.setLabels();
        this.setInput();
        this.setOutput();
        this.readDataToTable();
        if (args.length>0) { // don't check for updates in command line mode
            this.autorun(args);
        } else {
            Updater.run(this.data.preferences);
        }
    }
    
private void addFrameActions() {
    try {
        Action save = new AbstractAction("save"){
            @Override
            public void actionPerformed(ActionEvent e) {
                SavePlotMenuItemActionPerformed(e);
            }
        },
            close = new AbstractAction("close"){
            @Override
            public void actionPerformed(ActionEvent e) {
                quit();
            }
        },  open = new AbstractAction("open"){
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenMenuItemActionPerformed(e);
            }
        };
        KeyStroke saveStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK),
                  closeStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK),
                  openStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK);
        plotframe.plotpanel.getActionMap().put("save", save);
        plotframe.plotpanel.getActionMap().put("close", close);
        plotframe.plotpanel.getActionMap().put("open", open);
        plotframe.plotpanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveStroke, "save");
        plotframe.plotpanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeStroke, "close");
        plotframe.plotpanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(openStroke, "open");
    } catch (Exception e) {
        if (Data.DEBUGMODE) {e.printStackTrace(System.out);}
    }
}    

    private void autorun(String[] args) {
        try {
            String[] arg;
            double min = Data.NAN, max = Data.NAN,
                   central = Data.NAN, bandwidth = Data.NAN,
                   binwidth = Data.NAN, area = Data.NAN;
            String in = "", out = "", format = "pdf", markers = "";
            boolean plotandclose = false;
            // read the arguments and assign values to the input parameters
            for (String s: args) {
                try {
                    arg = s.split("=", 2);
                    if (arg[0].equalsIgnoreCase("min")) {
                        min = Double.parseDouble(arg[1]);
                    } else if (arg[0].equalsIgnoreCase("max")){
                        max = Double.parseDouble(arg[1]);
                    } else if (arg[0].equalsIgnoreCase("central")){
                        central = Double.parseDouble(arg[1]);
                    } else if (arg[0].equalsIgnoreCase("bandwidth")){
                        bandwidth = Double.parseDouble(arg[1]);
                    } else if (arg[0].equalsIgnoreCase("binwidth")){
                        binwidth = Double.parseDouble(arg[1]);
                    } else if (arg[0].equalsIgnoreCase("area")){
                        area = Double.parseDouble(arg[1]);
                    } else if (arg[0].equalsIgnoreCase("in")){
                        in = arg[1];
                    } else if (arg[0].equalsIgnoreCase("out")){
                        out = arg[1].split("\\.",2)[0];
                        format = arg[1].split("\\.",2)[1];
                        plotandclose = true;
                    } else if (arg[0].equalsIgnoreCase("markers")){
                        markers = arg[1];
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Incorrect input arguments");
                    if (Data.DEBUGMODE){e.printStackTrace(System.out);}
                }
            }
            // process the input parameters
            this.loadFile(in);        
            PlotPanel panel = this.plotframe.plotpanel;
            panel.refresh(data);
            if (min != Data.NAN & max != Data.NAN){
                panel.getPlot().initTimeScale(min, max);
                panel.getPlot().autoTimeScale(false);
            }
            if (central != Data.NAN){
                panel.getPlot().setCentralAge(central);
            }
            if (bandwidth > 0){
                panel.densityplot.autoBandwidth(false);
                panel.densityplot.kde.setBandwidth(bandwidth);
            }
            if (area > 0){
                panel.densityplot.autoArea(false);
                panel.densityplot.setArea(area);
            }
            if (binwidth > 0){
                panel.densityplot.autoBinWidth(false);
                panel.densityplot.setBinWidth(binwidth);
            }
            if (!markers.isEmpty()){
                panel.getPlot().plotMarkers(markers);
            }
            if (plotandclose){
                panel.refresh(data);
                savePlot(out,format);
                quit();
            }
        } catch (Exception e) {
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    public void savePlot(String out, String extension) throws Exception {
        PlotPanel panel = this.plotframe.plotpanel;
        panel.setDimension(2);      
        panel.repaint();        
        if (extension.equalsIgnoreCase("pdf")) {
            PDFGraphics2D g = new PDFGraphics2D(new File(out+"."+extension), this.plotframe.plotpanel.getSize());
            UserProperties p = new UserProperties();
            p.setProperty(PDFGraphics2D.TEXT_AS_SHAPES, false);
            g.setProperties(p);
            g.startExport();
            panel.print(g);
            g.endExport();           
        } else if (extension.equalsIgnoreCase("png")) {
            BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), 
                                                 BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            panel.print(g);
            ImageIO.write(bi, "PNG", new File(out+"."+extension));            
        } else if (extension.equalsIgnoreCase("csv")){
            panel.getPlot().plot2csv(out+"."+extension);
        }
        panel.setDimension(1);
        this.OpenRadialPlotFrame();         
    }

    /* sets the look to match the platform's OS (PC, Mac, Linux, ...)*/
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    private void setLookAndFeel() {
        try { 
          UIManager.setLookAndFeel(UIManager. 
            getSystemLookAndFeelClassName()); 
        } catch(Exception e) { 
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    // further initializations, including the creation of button groups
    private void myInit(){
        try {
            this.addWindowListener(new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e) {
                    Main.this.quit();
                    System.exit(0);
                }
            });
            data = new FTdata(new Preferences(DENSITYPLOTTER));
            ButtonGroup transformation = new ButtonGroup();
            transformation.add(this.LinearRadioButton);
            transformation.add(this.LogarithmicRadioButton);
            transformation.add(this.ArcsinRadioButton);
            ButtonGroup input = new ButtonGroup();
            input.add(this.FissionTracksRadioButton);
            input.add(this.OtherInputRadioButton);
            ButtonGroup output = new ButtonGroup();
            output.add(this.RadialPlotRadioButton);
            output.add(this.DensityPlotRadioButton);
            ButtonGroup peakfit = new ButtonGroup();
            peakfit.add(this.noPeaks);
            peakfit.add(this.onePeak);
            peakfit.add(this.twoPeaks);
            peakfit.add(this.threePeaks);
            peakfit.add(this.fourPeaks);
            peakfit.add(this.fivePeaks);
            peakfit.add(this.autoPeaks);
            peakfit.add(this.minimumAge);
            Preferences prefs = data.preferences;
            this.RadialPlotRadioButton.setSelected(prefs.radialplot());
            this.DensityPlotRadioButton.setSelected(prefs.densityplot());
            this.FissionTracksRadioButton.setSelected(prefs.fissiontracks());
            this.OtherInputRadioButton.setSelected(prefs.other());
            this.LinearRadioButton.setSelected(prefs.linear());
            this.LogarithmicRadioButton.setSelected(prefs.logarithmic());
            this.ArcsinRadioButton.setSelected(prefs.arcsin());
            // the next line prevents automatic editing when inserting and deleting rows
            DataTable.putClientProperty( "JTable.autoStartsEdit", false);
            this.setSize(400, 500);
            fc = this.initSavePlotFileChooser();
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }

    private void setInput() {
        try {
            if (data.preferences.fissiontracks()){
                this.setFissionTrackInput();
            } else {
                this.setOtherInput();
            }
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }

    private void setOutput(){
        try {
            if (data.preferences.radialplot()){ // radial plot
                this.ResizeTable(3);
                if (data.preferences.fissiontracks()){
                   this.ArcsinRadioButton.setVisible(true);
                } else if (data.preferences.other()) {
                   if (data.preferences.arcsin()){
                       data.preferences.transformation("linear");
                   }
                   this.ArcsinRadioButton.setVisible(false);
                }
            } else {             // density plot
                this.ResizeTable(2);
                this.ArcsinRadioButton.setVisible(false);
                if (data.preferences.arcsin()){
                    data.preferences.transformation("linear");
                }
            }
            this.LinearRadioButton.setSelected(data.preferences.linear());
            this.LogarithmicRadioButton.setSelected(data.preferences.logarithmic());
            this.ArcsinRadioButton.setSelected(data.preferences.arcsin());
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    private void setLabels(){
        try {
            if (data.preferences.fissiontracks()){
                this.ZetaDoseRateLabel.setText("\u03B6");
                this.zetaDoseUnitLabel.setText("yr cm\u00B2");
                this.rhoDlabel.setText("\u03C1D");
                this.rhoDunitLabel.setText("1/cm\u00B2");
            }
            this.setXlabel(data.preferences.xlabel());
            this.setYlabel(data.preferences.ylabel());
            if (data.preferences.radialplot()){
                this.setZlabel(data.preferences.zlabel());
            }
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    /** This method is called from within the constructor to
     * initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollpane = new javax.swing.JScrollPane();
        DataTable = new javax.swing.JTable();
        PlotButton = new javax.swing.JButton();
        samplename = new javax.swing.JTextField();
        ZetaDoseRate = new javax.swing.JTextField();
        SampleNameLabel = new javax.swing.JLabel();
        ZetaDoseRateLabel = new javax.swing.JLabel();
        rhoDlabel = new javax.swing.JLabel();
        ZetaDoseRateErr = new javax.swing.JTextField();
        ZetaDoseRatePMlabel = new javax.swing.JLabel();
        rhoD = new javax.swing.JTextField();
        rhoDpmLabel = new javax.swing.JLabel();
        rhoD_err = new javax.swing.JTextField();
        zetaDoseUnitLabel = new javax.swing.JLabel();
        rhoDunitLabel = new javax.swing.JLabel();
        menubar = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        NewMenuItem = new javax.swing.JMenuItem();
        OpenMenuItem = new javax.swing.JMenuItem();
        SaveDataMenuItem = new javax.swing.JMenuItem();
        SavePlotMenuItem = new javax.swing.JMenuItem();
        ExitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        CopyMenuItem = new javax.swing.JMenuItem();
        PasteMenuItem = new javax.swing.JMenuItem();
        InsertMenuItem = new javax.swing.JMenuItem();
        DeleteMenuItem = new javax.swing.JMenuItem();
        OptionMenu = new javax.swing.JMenu();
        InputMenuItem = new javax.swing.JMenu();
        FissionTracksRadioButton = new javax.swing.JRadioButtonMenuItem();
        OtherInputRadioButton = new javax.swing.JRadioButtonMenuItem();
        OutputMenuItem = new javax.swing.JMenu();
        RadialPlotRadioButton = new javax.swing.JRadioButtonMenuItem();
        DensityPlotRadioButton = new javax.swing.JRadioButtonMenuItem();
        TransformationMenuItem = new javax.swing.JMenu();
        LinearRadioButton = new javax.swing.JRadioButtonMenuItem();
        LogarithmicRadioButton = new javax.swing.JRadioButtonMenuItem();
        ArcsinRadioButton = new javax.swing.JRadioButtonMenuItem();
        PeakFitMenuItem = new javax.swing.JMenu();
        noPeaks = new javax.swing.JRadioButtonMenuItem();
        onePeak = new javax.swing.JRadioButtonMenuItem();
        twoPeaks = new javax.swing.JRadioButtonMenuItem();
        threePeaks = new javax.swing.JRadioButtonMenuItem();
        fourPeaks = new javax.swing.JRadioButtonMenuItem();
        fivePeaks = new javax.swing.JRadioButtonMenuItem();
        autoPeaks = new javax.swing.JRadioButtonMenuItem();
        minimumAge = new javax.swing.JRadioButtonMenuItem();
        ColourMenuItem = new javax.swing.JMenuItem();
        SetAxesMenuItem = new javax.swing.JMenuItem();
        HelpMenu = new javax.swing.JMenu();
        ContentsMenuItem = new javax.swing.JMenuItem();
        AboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        DataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Ns", "Ni", "[Dpar]"
            }
        ));
        scrollpane.setViewportView(DataTable);

        PlotButton.setText("Plot");
        PlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlotButtonActionPerformed(evt);
            }
        });

        samplename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samplenameEnterHandler(evt);
            }
        });
        samplename.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                samplenameFocusHandler(evt);
            }
        });

        ZetaDoseRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZetaDoseRateEnterHandler(evt);
            }
        });
        ZetaDoseRate.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ZetaDoseRateFocusHandler(evt);
            }
        });

        SampleNameLabel.setText("Sample Name");

        ZetaDoseRateLabel.setText("ζ");

        rhoDlabel.setText("ρD");

        ZetaDoseRateErr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zetaErrEnterHandler(evt);
            }
        });
        ZetaDoseRateErr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                zetaErrFocusHandler(evt);
            }
        });

        ZetaDoseRatePMlabel.setText("±");

        rhoD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rhoDenterHandler(evt);
            }
        });
        rhoD.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                rhoDfocusHandler(evt);
            }
        });

        rhoDpmLabel.setText("±");

        rhoD_err.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rhoDerrEnterHandler(evt);
            }
        });
        rhoD_err.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                rhoDerrFocusHandler(evt);
            }
        });

        zetaDoseUnitLabel.setText("yr cm²");

        rhoDunitLabel.setText("cm⁻²");

        jMenu1.setText("File");

        NewMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        NewMenuItem.setText("New");
        NewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(NewMenuItem);

        OpenMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        OpenMenuItem.setText("Open");
        OpenMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(OpenMenuItem);

        SaveDataMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        SaveDataMenuItem.setText("Save Data");
        SaveDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveDataMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(SaveDataMenuItem);

        SavePlotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        SavePlotMenuItem.setText("Save Plot");
        SavePlotMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SavePlotMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(SavePlotMenuItem);

        ExitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        ExitMenuItem.setText("Exit");
        ExitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitMenuActionPerformed(evt);
            }
        });
        jMenu1.add(ExitMenuItem);

        menubar.add(jMenu1);

        jMenu2.setText("Edit");

        CopyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        CopyMenuItem.setText("Copy");
        CopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(CopyMenuItem);

        PasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        PasteMenuItem.setText("Paste");
        PasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasteMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(PasteMenuItem);

        InsertMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        InsertMenuItem.setText("Insert");
        InsertMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InsertMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(InsertMenuItem);

        DeleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        DeleteMenuItem.setText("Delete");
        DeleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(DeleteMenuItem);

        menubar.add(jMenu2);

        OptionMenu.setText("Options");

        InputMenuItem.setText("Input");

        FissionTracksRadioButton.setText("Fission Tracks");
        FissionTracksRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FissionTracksRadioButtonActionPerformed(evt);
            }
        });
        InputMenuItem.add(FissionTracksRadioButton);

        OtherInputRadioButton.setText("Other");
        OtherInputRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OtherInputRadioButtonActionPerformed(evt);
            }
        });
        InputMenuItem.add(OtherInputRadioButton);

        OptionMenu.add(InputMenuItem);

        OutputMenuItem.setText("Output");

        RadialPlotRadioButton.setText("Radial Plot");
        RadialPlotRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadialPlotRadioButtonActionPerformed(evt);
            }
        });
        OutputMenuItem.add(RadialPlotRadioButton);

        DensityPlotRadioButton.setText("Density Plot");
        DensityPlotRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DensityPlotRadioButtonActionPerformed(evt);
            }
        });
        OutputMenuItem.add(DensityPlotRadioButton);

        OptionMenu.add(OutputMenuItem);

        TransformationMenuItem.setText("Transformation");

        LinearRadioButton.setText("Linear");
        LinearRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LinearRadioButtonActionPerformed(evt);
            }
        });
        TransformationMenuItem.add(LinearRadioButton);

        LogarithmicRadioButton.setText("Logarithmic");
        LogarithmicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogarithmicRadioButtonActionPerformed(evt);
            }
        });
        TransformationMenuItem.add(LogarithmicRadioButton);

        ArcsinRadioButton.setText("Arcsin");
        ArcsinRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ArcsinRadioButtonActionPerformed(evt);
            }
        });
        TransformationMenuItem.add(ArcsinRadioButton);

        OptionMenu.add(TransformationMenuItem);

        PeakFitMenuItem.setText("Mixture Models");

        noPeaks.setSelected(true);
        noPeaks.setText("none");
        noPeaks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noPeaksActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(noPeaks);

        onePeak.setText("1");
        onePeak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onePeakActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(onePeak);

        twoPeaks.setText("2");
        twoPeaks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoPeaksActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(twoPeaks);

        threePeaks.setText("3");
        threePeaks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                threePeaksActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(threePeaks);

        fourPeaks.setText("4");
        fourPeaks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fourPeaksActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(fourPeaks);

        fivePeaks.setText("5");
        fivePeaks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fivePeaksActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(fivePeaks);

        autoPeaks.setText("auto");
        autoPeaks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoPeaksActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(autoPeaks);

        minimumAge.setText("minimum");
        minimumAge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimumAgeActionPerformed(evt);
            }
        });
        PeakFitMenuItem.add(minimumAge);

        OptionMenu.add(PeakFitMenuItem);

        ColourMenuItem.setText("Colours");
        ColourMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColourMenuItemActionPerformed(evt);
            }
        });
        OptionMenu.add(ColourMenuItem);

        SetAxesMenuItem.setText("Settings");
        SetAxesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetAxesMenuItemActionPerformed(evt);
            }
        });
        OptionMenu.add(SetAxesMenuItem);

        menubar.add(OptionMenu);

        HelpMenu.setText("Help");

        ContentsMenuItem.setText("Contents");
        ContentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ContentsMenuItemActionPerformed(evt);
            }
        });
        HelpMenu.add(ContentsMenuItem);

        AboutMenuItem.setText("About");
        AboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutMenuItemActionPerformed(evt);
            }
        });
        HelpMenu.add(AboutMenuItem);

        menubar.add(HelpMenu);

        setJMenuBar(menubar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, SampleNameLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, ZetaDoseRateLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, rhoDlabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rhoD, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                            .add(ZetaDoseRate, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(ZetaDoseRatePMlabel)
                            .add(rhoDpmLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rhoD_err, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .add(ZetaDoseRateErr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(rhoDunitLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(zetaDoseUnitLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(samplename, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
                .addContainerGap())
            .add(scrollpane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(200, Short.MAX_VALUE)
                .add(PlotButton)
                .addContainerGap(201, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(SampleNameLabel)
                    .add(samplename, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ZetaDoseRateLabel)
                    .add(ZetaDoseRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ZetaDoseRatePMlabel)
                    .add(zetaDoseUnitLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ZetaDoseRateErr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rhoDlabel)
                    .add(rhoD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rhoDpmLabel)
                    .add(rhoD_err, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rhoDunitLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollpane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(PlotButton))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OpenRadialPlotFrame(){
        try {
            Rectangle r = this.getBounds();
            int x = r.x+r.width,
                y = r.y;
            plotframe.setLocation(x, y);
            plotframe.repaint();
            plotframe.setVisible(true);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }

    private void OpenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenMenuItemActionPerformed
        try {
            JFileChooser chooser = new JFileChooser(idir);
            int returnVal = chooser.showOpenDialog((Component) evt.getSource());
            if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                try {
                String filename = chooser.getSelectedFile().getAbsolutePath();
                idir = chooser.getCurrentDirectory().getAbsolutePath();
                clearDisplay();
                loadFile(filename);
                } catch (Exception e){
                    if (Data.DEBUGMODE){e.printStackTrace(System.out);}
                }
            }
        } catch (HeadlessException e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_OpenMenuItemActionPerformed

    protected void loadFile(String filename) {
        try {
            if (data.preferences.other() & Data.isFissionTrackFile(filename)) {
                this.FissionTracksRadioButton.doClick();
            }
            if (data.preferences.fissiontracks() & !Data.isFissionTrackFile(filename)) {
                this.OtherInputRadioButton.doClick();
            }
            if (data.preferences.fissiontracks()){
                data = new FTdata(filename,data.preferences);
            }
            if (data.preferences.other()){
                data = new OtherData(filename,data.preferences);
            }
            this.ArcSinTest();
            this.setOutput();
            readDataToTable();
        } catch (Exception e){
            System.out.println("Please specify a valid input file.");
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }

    protected PlotFrame getPlotFrame(){
        return this.plotframe;
    }

    private void clearDisplay(){
        try {
            samplename.setText("");
            ZetaDoseRate.setText("");
            ZetaDoseRateErr.setText("");
            rhoD.setText("");
            rhoD_err.setText("");
            for (int i=0; i<DataTable.getRowCount();i++){
                DataTable.setValueAt("", i, 0);
                DataTable.setValueAt("", i, 1);
                if (data.preferences.radialplot()){
                    DataTable.setValueAt("", i, 2);
                }
            }
            noPeaks.setSelected(true);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    private void PlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlotButtonActionPerformed
        try {
            this.readTableToData();
            this.ArcSinTest();
            plotframe.refresh(data);
            OpenRadialPlotFrame();
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_PlotButtonActionPerformed

    private void CopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyMenuItemActionPerformed
        try {
            clipboard.actionPerformed(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_CopyMenuItemActionPerformed

    private void PasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasteMenuItemActionPerformed
        try {
            clipboard.actionPerformed(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_PasteMenuItemActionPerformed

    private JFileChooser initSavePlotFileChooser() {
        fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setCurrentDirectory(new File(odir));
        String png = "*.png, *.PNG", pdf = "*.pdf, *.PDF", csv = "*.csv, *.CSV";
        FileNameExtensionFilter f1 = new FileNameExtensionFilter(pdf, "pdf", "PDF"),
                                f2 = new FileNameExtensionFilter(png, "png", "PNG"),
                                f3 = new FileNameExtensionFilter(csv, "csv", "CSV");            
        fc.addChoosableFileFilter(f1);
        fc.addChoosableFileFilter(f2);
        fc.addChoosableFileFilter(f3);
        fc.setFileFilter(f1);
        return fc;
    }
    
    private void SavePlotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SavePlotMenuItemActionPerformed
        try {
            if (odir.equals("")) {
                odir = idir.equals("") ? System.getProperty("user.dir") : idir;
            }
            String png = "*.png, *.PNG", pdf = "*.pdf, *.PDF", csv = "*.csv, *.CSV";
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                FileFilter fF = fc.getFileFilter();
                odir = fc.getSelectedFile().getParent() + File.separator;
                String fname = ToolBox.removeExtension(fc.getSelectedFile().getPath());
                if (fF.getDescription().equals(pdf)){
                    this.savePlot(odir+fname,"pdf");
                } else if (fF.getDescription().equals(png)) {
                    this.savePlot(odir+fname,"png");
                } else if (fF.getDescription().equals(csv)) {
                    this.savePlot(odir+fname,"csv");
                }
            }
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_SavePlotMenuItemActionPerformed
    
    private void samplenameFocusHandler(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_samplenameFocusHandler
        try {
            data.setSampleName(((JTextField)evt.getSource()).getText());
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_samplenameFocusHandler

    private void samplenameEnterHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samplenameEnterHandler
        try {
            data.setSampleName(((JTextField)evt.getSource()).getText());
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_samplenameEnterHandler

    private void ZetaDoseRateEnterHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZetaDoseRateEnterHandler
        try {
            this.ZetaDoseRateHandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_ZetaDoseRateEnterHandler

    private void ZetaDoseRateFocusHandler(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ZetaDoseRateFocusHandler
        try {
            this.ZetaDoseRateHandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_ZetaDoseRateFocusHandler

    private void ZetaDoseRateHandler(EventObject evt){
        try {
            ((FTdata)data).setZeta(Double.parseDouble(((JTextField)evt.getSource()).getText()));
        }catch(NumberFormatException e){
            System.out.println(e.toString());
        }        
    }
    
    private void zetaErrEnterHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zetaErrEnterHandler
        try {
            this.zetaErrHandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_zetaErrEnterHandler

    private void zetaErrFocusHandler(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_zetaErrFocusHandler
        try {
            this.zetaErrHandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_zetaErrFocusHandler

    private void zetaErrHandler(EventObject evt){
        try {
            ((FTdata)data).setZeta_Err(Double.parseDouble(((JTextField)evt.getSource()).getText()));
        } catch (NumberFormatException e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }        
    }
    
    private void rhoDenterHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rhoDenterHandler
        try {
            this.rhoDhandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_rhoDenterHandler

    private void rhoDfocusHandler(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rhoDfocusHandler
        try {
            this.rhoDhandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_rhoDfocusHandler

    private void rhoDhandler(EventObject evt){
        try {
            ((FTdata)data).setRhoD(Double.parseDouble(((JTextField)evt.getSource()).getText()));
        } catch (NumberFormatException e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    private void rhoDerrEnterHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rhoDerrEnterHandler
        try {
            this.rhoDerrHandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_rhoDerrEnterHandler

    private void rhoDerrFocusHandler(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_rhoDerrFocusHandler
        try {
            this.rhoDerrHandler(evt);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_rhoDerrFocusHandler

    private void rhoDerrHandler(EventObject evt){
        try {
            ((FTdata)data).setRhoD_Err(Double.parseDouble(((JTextField)evt.getSource()).getText()));
        } catch (NumberFormatException e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }
    
    private void SaveDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveDataMenuItemActionPerformed
        JFileChooser chooser;
        int returnVal;

        try {
            chooser = new JFileChooser(idir);
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "*.csv";
                }
            };
            chooser.setFileFilter(filter);           
            String sname = this.getSampleName(),
                   fname = sname.equals("") ? "*.csv" : sname + ".csv";
            chooser.setSelectedFile(new File(fname));
            returnVal = chooser.showSaveDialog((Component) evt.getSource());

            if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                String filepath = chooser.getSelectedFile().getAbsolutePath();
                idir = chooser.getCurrentDirectory().getAbsolutePath();
                this.readTableToData();
                data.writeOutput(filepath);
            }
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_SaveDataMenuItemActionPerformed

    private void ExitMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitMenuActionPerformed
        quit();
}//GEN-LAST:event_ExitMenuActionPerformed

  private void quit() {
    try {
      if (!this.data.preferences.saveprefs()) {
        this.data.preferences.delete();
      }
      System.exit(0);
    } catch (Exception ex) {
      if (Data.DEBUGMODE){ex.printStackTrace(System.out);}
      System.exit(1);
    }
  }

    private void LinearRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LinearRadioButtonActionPerformed
        try {
            data.preferences.transformation("linear");
            data.preferences.sigmalines(false);
            transform(data);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_LinearRadioButtonActionPerformed

    private void LogarithmicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogarithmicRadioButtonActionPerformed
        try {
            data.preferences.transformation("logarithmic");
            transform(data);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_LogarithmicRadioButtonActionPerformed

    private void ArcsinRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ArcsinRadioButtonActionPerformed
        try {
            data.preferences.transformation("arcsin");
            transform(data);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_ArcsinRadioButtonActionPerformed

    private void transform(Data data) throws Exception {
        PlotPanel panel = plotframe.plotpanel;
        if (data.preferences.radialplot()){
            panel.radialplot.data = data;
            panel.radialplot.resetKDE(); 
        } else {
            panel.densityplot.data = data;
            panel.densityplot.setDefault();
        }       
    }
    
    private void InsertMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InsertMenuItemActionPerformed
        try {
            int r = DataTable.getSelectedRow();
            this.insertRow(r);
            plotframe.getPlotPanel().getRadialPlot().insertEntry(Data.NAN, Data.NAN, Data.NAN, r);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_InsertMenuItemActionPerformed

    private void insertRow(int i) throws Exception {
        if (data.preferences.fissiontracks()){
            table.insertRow(i, new Object[]{" "," "," "});
        } else {
            table.insertRow(i, new Object[]{" "," "});
        }
        lineTable.setModel(table);
    }

    private void DeleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteMenuItemActionPerformed
        try {
            int r = DataTable.getSelectedRow();
            table.removeRow(r);
            plotframe.getPlotPanel().getRadialPlot().removeEntry(r);
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_DeleteMenuItemActionPerformed

    private void SetAxesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetAxesMenuItemActionPerformed
        try {
            if (this.RadialPlotRadioButton.isSelected()) {
                RadialOptions.createAndShowGUI(this);
            } else {
                DensityOptions.createAndShowGUI(this);
            }
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_SetAxesMenuItemActionPerformed
    
    private void ColourMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColourMenuItemActionPerformed
        try {
            if (data.preferences.radialplot()){
                ColourChooser.createAndShowGUI(plotframe.getPlotPanel().getRadialPlot());
            } else {
                DensityColourChooser.createAndShowGUI(plotframe.getPlotPanel().getDensityPlot());
            }
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
}//GEN-LAST:event_ColourMenuItemActionPerformed

    private void FissionTracksRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FissionTracksRadioButtonActionPerformed
        try {
            data.preferences.input("fissiontracks");
            data.preferences.setLabels();
            setFissionTrackInput();
            plotframe.plotpanel.densityplot.setDefault();
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_FissionTracksRadioButtonActionPerformed

    private void OtherInputRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OtherInputRadioButtonActionPerformed
        try {
            data.preferences.input("other");
            data.preferences.setLabels();
            setOtherInput();
            plotframe.plotpanel.densityplot.setDefault();
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_OtherInputRadioButtonActionPerformed

    private void setFissionTrackInput() throws Exception {
        this.ZetaDoseRateLabel.setVisible(true);
        this.rhoDlabel.setVisible(true);
        this.ZetaDoseRatePMlabel.setVisible(true);
        this.rhoDpmLabel.setVisible(true);
        this.ZetaDoseRate.setVisible(true);
        this.ZetaDoseRateErr.setVisible(true);
        this.zetaDoseUnitLabel.setVisible(true);
        this.rhoD.setVisible(true);
        this.rhoD_err.setVisible(true);
        this.rhoDunitLabel.setVisible(true);
        this.ArcsinRadioButton.setVisible(true);
        this.setLabels();
        data = FTdata.cast(data);
    }

    private void setOtherInput() throws Exception {
        this.ZetaDoseRateLabel.setVisible(false);
        this.rhoDlabel.setVisible(false);
        this.ZetaDoseRatePMlabel.setVisible(false);
        this.zetaDoseUnitLabel.setVisible(false);
        this.rhoDpmLabel.setVisible(false);
        this.ZetaDoseRate.setVisible(false);
        this.ZetaDoseRateErr.setVisible(false);
        this.rhoD.setVisible(false);
        this.rhoD_err.setVisible(false);
        this.rhoDunitLabel.setVisible(false);
        this.ArcsinRadioButton.setVisible(false);
        this.setLabels();
        data = OtherData.cast(data);
    }

    public void setXlabel(String val) throws Exception{
        data.preferences.xlabel(val);
        this.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(val);
    }

    public void setYlabel(String val) throws Exception{
        data.preferences.ylabel(val);
        this.getTableHeader().getColumnModel().getColumn(1).setHeaderValue(val);
    }

    public void setZlabel(String val) throws Exception{
        data.preferences.zlabel(val);
        this.getTableHeader().getColumnModel().getColumn(2).setHeaderValue(val);
    }

    private void NewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewMenuItemActionPerformed
        try {
            clearDisplay();
            String filename = "";
            if (data.preferences.fissiontracks()){
                data = new FTdata(filename,data.preferences);
            } else {
                data = new OtherData(filename,data.preferences);
            }
            this.setOutput();
        } catch (Exception e){
            if (Data.DEBUGMODE){e.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_NewMenuItemActionPerformed

private void ContentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ContentsMenuItemActionPerformed
    try {
        openHelp();
    } catch (Exception e){
        if (Data.DEBUGMODE){e.printStackTrace(System.out);}
    }
}//GEN-LAST:event_ContentsMenuItemActionPerformed

private void AboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutMenuItemActionPerformed
    String url = Updater.getURL();
    String message = Updater.getProgramName(VERSION) + 
                    "<br><br>Pieter Vermeesch<br>" +
                    "<br>London Geochronology Centre<br>" + 
                    "<br><a href=\"\">" + url + "</a><br>";
    JEditorPane ep = ToolBox.myJEditorPane(message,url,false);
    JOptionPane.showMessageDialog(this, ep); 
}//GEN-LAST:event_AboutMenuItemActionPerformed

private void noPeaksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noPeaksActionPerformed
    setPeaks(0);
}//GEN-LAST:event_noPeaksActionPerformed

private void onePeakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onePeakActionPerformed
    setPeaks(1);
}//GEN-LAST:event_onePeakActionPerformed

private void twoPeaksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoPeaksActionPerformed
    setPeaks(2);
}//GEN-LAST:event_twoPeaksActionPerformed

private void threePeaksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_threePeaksActionPerformed
    setPeaks(3);
}//GEN-LAST:event_threePeaksActionPerformed

private void fourPeaksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fourPeaksActionPerformed
    setPeaks(4);
}//GEN-LAST:event_fourPeaksActionPerformed

private void fivePeaksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fivePeaksActionPerformed
    setPeaks(5);
}//GEN-LAST:event_fivePeaksActionPerformed

private void autoPeaksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoPeaksActionPerformed
    try {
        BinomFit binomfit;
        if (data.preferences.radialplot()){
            binomfit = this.plotframe.getPlotPanel().getRadialPlot().getBinomFit();
        } else {
            binomfit = this.plotframe.getPlotPanel().getDensityPlot().getBinomFit();
        }
        binomfit.auto();
    } catch (Exception e) {
        if (Data.DEBUGMODE){e.printStackTrace(System.out);}
    }
}//GEN-LAST:event_autoPeaksActionPerformed

private void RadialPlotRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadialPlotRadioButtonActionPerformed
    try {
        data.preferences.output("radialplot");
        this.setOutput();
        ArcSinTest();
        plotframe.plotpanel.radialRefresh(data);
    } catch (Exception e) {
        if (Data.DEBUGMODE){e.printStackTrace(System.out);}
    }
}//GEN-LAST:event_RadialPlotRadioButtonActionPerformed

private void DensityPlotRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DensityPlotRadioButtonActionPerformed
    try {
        data.preferences.output("densityplot");
        this.setOutput();
        plotframe.plotpanel.densityRefresh(data);
    } catch (Exception e) {
        if (Data.DEBUGMODE){e.printStackTrace(System.out);}
    }
}//GEN-LAST:event_DensityPlotRadioButtonActionPerformed

private void minimumAgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimumAgeActionPerformed
    setPeaks(-1);
}//GEN-LAST:event_minimumAgeActionPerformed

private void ArcSinTest() throws Exception {
    if (data.preferences.fissiontracks() & data.hasZeros()){
        if (this.RadialPlotRadioButton.isSelected()){
            this.ArcsinRadioButton.doClick();
        } else {
            this.LinearRadioButton.doClick();
        }
    }    
}

private void ResizeTable(int size) throws Exception {
    Data backup = data;
    table = new DefaultTableModel(DataTable.getRowCount(), size);
    DataTable.setModel(table);
    this.setLabels();
    data = backup;
    this.readDataToTable();
}

private void setPeaks(int num){
    try {
        BinomFit binomfit;
        if (data.preferences.radialplot()){
            binomfit = this.plotframe.getPlotPanel().getRadialPlot().getBinomFit();
        } else {
            binomfit = this.plotframe.getPlotPanel().getDensityPlot().getBinomFit();
        }
        binomfit.setNumPeaks(num);
        binomfit.findPeaks(num);
    } catch (Exception e){
        if (Data.DEBUGMODE){e.printStackTrace(System.out);}
    }
}

private void readDataToTable(){
    try {
        int i = 0;
        samplename.setText(data.getSampleName());
        if (data.preferences.fissiontracks()){
            double zdr  = ((FTdata)data).getZeta(),
                   zdre = ((FTdata)data).getZeta_Err(),
                   rhd  = ((FTdata)data).getRhoD(),
                   rhde = ((FTdata)data).getRhoD_Err();
            String ZDR  = (zdr > 0) ? String.valueOf(zdr) : "",
                   ZDRE = (zdre > 0) ? String.valueOf(zdre) : "",
                   RHD  = (rhd > 0) ? String.valueOf(rhd) : "",
                   RHDE = (rhde > 0) ? String.valueOf(rhde) : "";
            ZetaDoseRate.setText(ZDR);
            ZetaDoseRateErr.setText(ZDRE);
            rhoD.setText(RHD);
            rhoD_err.setText(RHDE);
        }
        double[] XYW;
        for (Iterator ii = data.iterator(); ii.hasNext(); ) {
            if (i>=table.getRowCount()){
                this.insertRow(i);
            }
            XYW = (double[]) ii.next();
            if (data.preferences.fissiontracks() & XYW[1] >= 0){
                table.setValueAt((int)XYW[0], i, 0);
                table.setValueAt((int)XYW[1], i, 1);
            }
            if (!data.preferences.fissiontracks()){
                table.setValueAt(XYW[0], i, 0);
                if (XYW[1] >= 0){
                    table.setValueAt(XYW[1], i, 1);
                }
            }
            if (XYW[2] >= 0 & data.preferences.radialplot()){
                table.setValueAt(XYW[2], i, 2);
            }
            i++;
        }
    } catch (Exception e){
        if (Data.DEBUGMODE){e.printStackTrace(System.out);}
    }
}

public void readTableToData() throws Exception{
    double val;
    data.clear();
    for (int r=0; r<table.getRowCount(); r++){
        for (int c=0; c<table.getColumnCount(); c++){
            val = getCellValue(r,c);
            switch (c) {
                case 0:
                    data.setX(val, r);
                    break;
                case 1:
                    data.setY(val, r);
                    break;
                case 2:
                    data.setW(val, r);
                    break;
                default:
                    break;
            }
        }
    }
}

    @SuppressWarnings("FinallyDiscardsException")
    private double getCellValue(int r, int c){
    double val = Data.NAN;
    try {
        if (table.getValueAt(r,c).getClass().toString().equals("class java.lang.String")){
            val = Double.parseDouble((String) table.getValueAt(r, c));
        } else if ((table.getValueAt(r, c).getClass().toString().equals("class java.lang.Integer"))){
            val = (double) ((int)((Integer) table.getValueAt(r, c)));
        } else if ((table.getValueAt(r, c).getClass().toString().equals("class java.lang.Double"))){
            val = (double) ((Double) table.getValueAt(r, c));
        }
    } catch (NumberFormatException e){
        val = Data.NAN;
    } finally {
        return val;
    }
}

public JTableHeader getTableHeader() throws Exception{
    return DataTable.getTableHeader();
}

private String getSampleName() throws Exception {
    PlotPanel panel = this.getPlotFrame().getPlotPanel();
    if (this.data.preferences.radialplot()) {
        return panel.getRadialPlot().getData().getSampleName();
    } else {
        return panel.getDensityPlot().getData().getSampleName();
    }
}

/**This method opens up the help viewer for specified help set 
* and displays the home ID of that help set
*/
public void openHelp() {
    // Identify the location of the .hs file
    String pathToHS = "/RadialHelp/docs/radialhelp-hs.xml";
    //Create a URL for the location of the help set
    try {
        hsURL = this.getClass().getResource(pathToHS);
        hs = new HelpSet(null, hsURL);
    } catch (HelpSetException ee) {
        // Print info to the console if there is an exception
        System.out.println( "HelpSet " + ee.getMessage());
        System.out.println("Help Set "+ pathToHS +" not found");
        return;
    }

    // Create a HelpBroker object for manipulating the help set
    hb = hs.createHelpBroker();
    //Display help set
    hb.setDisplayed(true);
}    

public static void main(final String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
            new Main(args).setVisible(true);
        }
    });
}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AboutMenuItem;
    private javax.swing.JRadioButtonMenuItem ArcsinRadioButton;
    private javax.swing.JMenuItem ColourMenuItem;
    private javax.swing.JMenuItem ContentsMenuItem;
    private javax.swing.JMenuItem CopyMenuItem;
    private javax.swing.JTable DataTable;
    private javax.swing.JMenuItem DeleteMenuItem;
    private javax.swing.JRadioButtonMenuItem DensityPlotRadioButton;
    private javax.swing.JMenuItem ExitMenuItem;
    private javax.swing.JRadioButtonMenuItem FissionTracksRadioButton;
    private javax.swing.JMenu HelpMenu;
    private javax.swing.JMenu InputMenuItem;
    private javax.swing.JMenuItem InsertMenuItem;
    private javax.swing.JRadioButtonMenuItem LinearRadioButton;
    private javax.swing.JRadioButtonMenuItem LogarithmicRadioButton;
    private javax.swing.JMenuItem NewMenuItem;
    private javax.swing.JMenuItem OpenMenuItem;
    private javax.swing.JMenu OptionMenu;
    private javax.swing.JRadioButtonMenuItem OtherInputRadioButton;
    private javax.swing.JMenu OutputMenuItem;
    private javax.swing.JMenuItem PasteMenuItem;
    private javax.swing.JMenu PeakFitMenuItem;
    private javax.swing.JButton PlotButton;
    private javax.swing.JRadioButtonMenuItem RadialPlotRadioButton;
    private javax.swing.JLabel SampleNameLabel;
    private javax.swing.JMenuItem SaveDataMenuItem;
    private javax.swing.JMenuItem SavePlotMenuItem;
    private javax.swing.JMenuItem SetAxesMenuItem;
    private javax.swing.JMenu TransformationMenuItem;
    private javax.swing.JTextField ZetaDoseRate;
    private javax.swing.JTextField ZetaDoseRateErr;
    private javax.swing.JLabel ZetaDoseRateLabel;
    private javax.swing.JLabel ZetaDoseRatePMlabel;
    private javax.swing.JRadioButtonMenuItem autoPeaks;
    private javax.swing.JRadioButtonMenuItem fivePeaks;
    private javax.swing.JRadioButtonMenuItem fourPeaks;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar menubar;
    private javax.swing.JRadioButtonMenuItem minimumAge;
    private javax.swing.JRadioButtonMenuItem noPeaks;
    private javax.swing.JRadioButtonMenuItem onePeak;
    private javax.swing.JTextField rhoD;
    private javax.swing.JTextField rhoD_err;
    private javax.swing.JLabel rhoDlabel;
    private javax.swing.JLabel rhoDpmLabel;
    private javax.swing.JLabel rhoDunitLabel;
    private javax.swing.JTextField samplename;
    private javax.swing.JScrollPane scrollpane;
    private javax.swing.JRadioButtonMenuItem threePeaks;
    private javax.swing.JRadioButtonMenuItem twoPeaks;
    private javax.swing.JLabel zetaDoseUnitLabel;
    // End of variables declaration//GEN-END:variables
    protected PlotFrame plotframe;
    protected Data data;
    private DefaultTableModel table;
    ExcelAdapter clipboard;
    private HelpSet hs;
    private HelpBroker hb;
    private URL hsURL;
    private JFileChooser fc;
    protected LineNumberTable lineTable;
    static final boolean DENSITYPLOTTER = true;
    static final String VERSION = "7.2";
    private String idir = "", odir = "";
    
}