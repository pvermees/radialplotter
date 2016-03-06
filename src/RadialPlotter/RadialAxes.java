package RadialPlotter;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class RadialAxes {
    
    RadialAxes(RadialPlot plot) throws Exception {
        this.plot = plot;
        this.prefs = plot.getData().preferences;
        this.precisionticks = new ArrayList();
        this.relerrticks = new ArrayList();
        this.bottommargin = 3*plot.BOTTOMMARGIN/5;
    }

    public void plot() throws Exception {
        this.plotXaxis();
        this.plotYaxis();
    }

    private void plotYaxis() throws Exception {
        double[] xy0 = plot.getOrigin();
        double[] xy1 = plot.rxry2xy(0, 2);
        double[] xy2 = plot.rxry2xy(0, -2);
        double[] xy3 = plot.rxry2xy(0, 1);
        double[] xy4 = plot.rxry2xy(0, -1);
        // plot the actual y-axis
        plot.getGraphics().drawLine(plot.wmap(xy1[0]), plot.hmap(xy1[1]), 
                    plot.wmap(xy2[0]), plot.hmap(xy2[1]));
        // plot the ticks
        plot.getGraphics().drawLine(plot.wmap(xy0[0]), plot.hmap(xy0[1]), 
                    plot.wmap(xy0[0]-plot.getTicklength()), plot.hmap(xy0[1]));
        plot.getGraphics().drawLine(plot.wmap(xy1[0]), plot.hmap(xy1[1]), 
                    plot.wmap(xy1[0]-plot.getTicklength()), plot.hmap(xy1[1]));
        plot.getGraphics().drawLine(plot.wmap(xy2[0]), plot.hmap(xy2[1]), 
                    plot.wmap(xy2[0]-plot.getTicklength()), plot.hmap(xy2[1]));
        plot.getGraphics().drawLine(plot.wmap(xy3[0]), plot.hmap(xy3[1]), 
                    plot.wmap(xy3[0]-plot.getTicklength()/2), plot.hmap(xy3[1]));
        plot.getGraphics().drawLine(plot.wmap(xy4[0]), plot.hmap(xy4[1]), 
                    plot.wmap(xy4[0]-plot.getTicklength()/2), plot.hmap(xy4[1]));
        plot.getGraphics().drawString(" 0", plot.wmap(xy0[0]-3*plot.getTicklength()), 
                                           plot.hmap(xy0[1]-plot.getFontSize()/2));
        plot.getGraphics().drawString(" 2", plot.wmap(xy1[0]-3*plot.getTicklength()), 
                                           plot.hmap(xy1[1]-plot.getFontSize()/2));
        plot.getGraphics().drawString("-2", plot.wmap(xy2[0]-3*plot.getTicklength()), 
                                           plot.hmap(xy2[1]-plot.getFontSize()/2));
    }
    
    private void plotXaxis() throws Exception {
        this.setPrecisionTicks();
        this.setRelErrTicks();
        this.plotXaxisLine();
        if (prefs.logarithmic() || prefs.arcsin()){
            this.plotPrecisionTicks();
        }
        if (prefs.linear() || prefs.logarithmic()){
            this.plotRelErrTicks();
        }
    }
    
    private void plotXaxisLine() throws Exception {
        double dummy = 0; // the value of the y-variable is not important
        double[] xy1, xy2, xy3;
        Graphics2D g2 = plot.getGraphics();
        xy1 = plot.rxry2xy(precisionticks.get(0), dummy);
        if (prefs.linear()){
            xy2 = plot.rxry2xy(1/relerrticks.get(0), dummy);
            g2.drawLine(plot.wmap(xy1[0]), plot.hmap(bottommargin), 
                plot.wmap(xy2[0]), plot.hmap(bottommargin));
            // add tick at beginning of x-axis.
            g2.drawLine(plot.wmap(xy1[0]), plot.hmap(bottommargin), 
                plot.wmap(xy1[0]), plot.hmap(bottommargin+plot.getTicklength()));
            g2.drawString("σ", plot.wmap(2*xy1[0]/3), plot.hmap(bottommargin+plot.getTicklength()));
        } else if (prefs.logarithmic()){
            xy2 = plot.rxry2xy(precisionticks.get(precisionticks.size()-1), dummy);
            xy3 = plot.rxry2xy(1/relerrticks.get(0), dummy);
            if (xy3[0]>xy2[0]){
                g2.drawLine(plot.wmap(xy1[0]), plot.hmap(bottommargin),
                plot.wmap(xy3[0]), plot.hmap(bottommargin));                    
            } else {
                g2.drawLine(plot.wmap(xy1[0]), plot.hmap(bottommargin), 
                    plot.wmap(xy2[0]), plot.hmap(bottommargin));
            }
            // add tick at beginning of x-axis.
            g2.drawLine(plot.wmap(xy1[0]), plot.hmap(bottommargin), 
                plot.wmap(xy1[0]), plot.hmap(bottommargin+plot.getTicklength()));
            double offset = plot.data.offset, 
                   ytop = 2*bottommargin/3,
                   ybot = bottommargin+plot.getTicklength();
            String label = "t";
            if (offset>0){
                label = "(t+" + String.valueOf(offset) + ")";
                ytop -= plot.getLineHeight();
                ybot += plot.getLineHeight();
            }
            g2.drawString(label + "/σ", plot.wmap(xy1[0]/3), plot.hmap(ytop));
            g2.drawString("σ/" + label, plot.wmap(xy1[0]/3), plot.hmap(ybot));
        } else if (prefs.arcsin()){
            xy2 = plot.rxry2xy(precisionticks.get(precisionticks.size()-1), dummy);
            g2.drawLine(plot.wmap(xy1[0]), plot.hmap(bottommargin), 
                plot.wmap(xy2[0]), plot.hmap(bottommargin));
            g2.drawString("Ns+Ni", plot.wmap(xy1[0]/50), plot.hmap(2*bottommargin/3));
        }
        // uncomment the following two lines if you want the x-axis to be longer
        //plot.getGraphics().drawLine(plot.wmap(xy[0]), plot.hmap(bottommargin), 
        //                            plot.wmap(1-plot.RIGHTMARGIN), plot.hmap(bottommargin));
    }
    
    private void plotPrecisionTicks() throws Exception {
        double[] xy;
        double dummy = 0; // the value of the y-variable is not important
        String label;
        for (int i=0; i<precisionticks.size(); i++){
            xy = plot.rxry2xy(precisionticks.get(i), dummy);
            plot.getGraphics().drawLine(plot.wmap(xy[0]), plot.hmap(bottommargin), 
                                    plot.wmap(xy[0]), plot.hmap(bottommargin-plot.getTicklength()));
            label = getPrecisionLabel(precisionticks.get(i));
            plot.getGraphics().drawString(label,plot.wmap(xy[0]),plot.hmap(2*bottommargin/3));
        }
    }
    
    private String getPrecisionLabel(double precision) throws Exception {
        if (prefs.logarithmic()){
            return Integer.toString((int)precision); // x
        } else if (prefs.arcsin()){
            return Integer.toString((int)(precision*precision/4)); // Ns + Ni
        } else {
            return "";
        }
    }
    
    private void plotRelErrTicks() throws Exception {
        double[] xy;
        double dummy = 0; // the value of the y-variable is not important
        String label;
        Data data = plot.getData();
        for (int i=0; i<relerrticks.size(); i++){
            xy = plot.rxry2xy(1/relerrticks.get(i), dummy);
            plot.getGraphics().drawLine(plot.wmap(xy[0]), plot.hmap(bottommargin), 
                                    plot.wmap(xy[0]), plot.hmap(bottommargin+plot.getTicklength()));
            label = this.getRelErrLabel(relerrticks.get(i));
            if (i==0){
                if (prefs.linear() && (data instanceof FTdata)){
                    label += (plot.unit == Data.MA ? "Ma" :
                              (plot.unit == Data.KA ? "ka" : "yr"));
                } else if (prefs.logarithmic()){
                    label += "%";
                }
            }
            plot.getGraphics().drawString(label,plot.wmap(xy[0]),
                plot.hmap(bottommargin+plot.getTicklength()));
        }
    }

    private String getRelErrLabel(double relerr) throws Exception {
        if (prefs.linear()){
            return ToolBox.num2string(relerr/plot.unit, 0);
        } else if (prefs.logarithmic()){
            return ToolBox.num2string(100*relerr,0);
        } else {
            return "";
        }
    }    
    
    private void setPrecisionTicks() throws Exception {
        this.precisionticks.clear();
        double range = Math.ceil(ToolBox.getMax(plot.getRadialX()));
        double interval = getPrecisionInterval(range);
        precisionticks.add(0, 0d);
        for (int i=1;(precisionticks.get(i-1)+interval)<range;i++){
            precisionticks.add(precisionticks.get(i-1)+interval);
        }
        precisionticks.add(precisionticks.size(), range);
    }
    
    private double getPrecisionInterval(double range) throws Exception {
        if (range <= 5) {
            return 1;
        } else {
            return Math.floor(range/5.0);
        }
    }

    private void setRelErrTicks() throws Exception {
        this.relerrticks.clear();
        double maxx = ToolBox.getMax(plot.getRadialX()),
               minx = ToolBox.getMin(plot.getRadialX());
        if (prefs.linear() | prefs.logarithmic()){
            relerrticks.add(0, 2d/(minx+maxx));
            relerrticks.add(0, 1d/minx);
            relerrticks.add(0, 1d/maxx);
        } else {
            relerrticks.add(0, 1d);
        }
    }

    protected RadialPlot plot;
    protected ArrayList<Double> precisionticks, relerrticks;
    protected double bottommargin;
    protected Preferences prefs;

}