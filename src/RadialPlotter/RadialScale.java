package RadialPlotter;

import java.util.ArrayList;

public class RadialScale {
    
    RadialScale(RadialPlot plot) throws Exception {
        this.plot = plot;
        this.tickages = new ArrayList();
    }

    // first plot the ticks, then the circular section
    public void plot() throws Exception {
        this.addTicks();
        double[] origin = plot.getOrigin();
        // ulx = upper left x; uly = upper left y
        int ulx = plot.wmap(origin[0]) - plot.wmap(plot.get_r0());
        int uly = plot.hmap(origin[1]) - plot.wmap(plot.get_r0());
        int diam = plot.wmap(2*plot.get_r0());
        double minz = plot.t2z(this.tickages.get(0));
        double maxz = plot.t2z(this.tickages.get(this.tickages.size()-1));
        double[] minxy = plot.rz2xy(plot.get_r0(), minz);
        double[] maxxy = plot.rz2xy(plot.get_r0(), maxz);
        double minxpix = plot.wmap(minxy[0])-plot.wmap(origin[0]);
        double minypix = plot.hmap(minxy[1])-plot.hmap(origin[1]);
        int alpha = (int)(Math.ceil(Math.atan(minypix/minxpix)*180/Math.PI));
        double maxxpix = plot.wmap(maxxy[0])-plot.wmap(origin[0]);
        double maxypix = plot.hmap(origin[1])-plot.hmap(maxxy[1]);
        int beta = (int)(Math.ceil((Math.atan(maxypix/maxxpix)*180/Math.PI)));
        plot.getGraphics().drawArc(ulx, uly, diam, diam, -alpha, alpha+beta);
    }

    private void addTicks() throws Exception {
        this.setTickAges();
        String[] labels;
        if (!plot.fixunits){ plot.autoUnit(); }
        labels = getLabels(this.tickages);
        for (int i=0;i<this.tickages.size();i++){
            drawTick(this.tickages.get(i),labels[i]);
        }
    }

    private String[] getLabels(ArrayList<Double> tickages) throws Exception {
        int numticks = tickages.size();
        String[] labels = new String[numticks];
        String unitlabel = "";
        Data data = plot.getData();
        if (data instanceof OtherData){
            unitlabel = "";
        } else if (plot.unit == Data.MA) {
            unitlabel = "Ma";
        } else if (plot.unit == Data.KA) {
            unitlabel = "ka";
        } else if (plot.unit == Data.A) {
            unitlabel = "";
        }
        double diff;
        int sigdig = 1;
        for (int i=0; i<numticks-1; i++){
            diff = (tickages.get(i+1) - tickages.get(i)) / plot.unit;
            sigdig = (diff>0) ? (int) Math.abs(Math.floor(ToolBox.log10(diff))) : 1;
            labels[i] = ToolBox.num2string(tickages.get(i)/plot.unit, sigdig);
        }
        labels[0] += unitlabel;
        labels[numticks-1] = ToolBox.num2string(tickages.get(numticks-1)/plot.unit, sigdig) + unitlabel;
        return labels;
    }
    
    private void setTickAges() throws Exception {
        this.tickages.clear();
        double[] minmaxt = plot.getMinMaxAge();
        double interval, range = minmaxt[1]-minmaxt[0];
        double exponent = Math.floor(ToolBox.log10(range));
        double r = ToolBox.log10(range) - exponent;
        if ((r<=1) && (r>ToolBox.log10(5))){
            interval = Math.pow(10, exponent);
        } else if ((r<=ToolBox.log10(5)) && (r>ToolBox.log10(2))){
            interval = 5 * Math.pow(10, exponent - 1);
        } else {
            interval = 2 * Math.pow(10, exponent - 1);
        }
        double lowert = Math.floor(minmaxt[0]/interval)*interval;
        double uppert = Math.ceil(minmaxt[1]/interval)*interval;
        this.tickages.add(lowert);
        int i;
        for (i=1;(this.tickages.get(i-1)+interval)<uppert;i++){
            this.tickages.add(this.tickages.get(i-1)+interval);
        }
        this.tickages.set(0,minmaxt[0]); // replace lowert
        this.tickages.set(i-1,minmaxt[1]); // replace lowert
    }

    private void drawTick(double t, String label) throws Exception {
        double z = plot.t2z(t);
        double[] xy1 = plot.rz2xy(plot.r0, z);
        double[] xy2 = plot.rz2xy(plot.r0-plot.getTicklength(),z);
        plot.getGraphics().drawLine(plot.wmap(xy1[0]), plot.hmap(xy1[1]), 
                                    plot.wmap(xy2[0]), plot.hmap(xy2[1]));
        double x, y;
        int width = 0;
        if (plot.data.preferences.abanico()){
            width = plot.g2.getFontMetrics().stringWidth(label);
            x = xy2[0];
            y = xy2[1];
        } else {
            x = xy1[0] + plot.getTicklength();
            y = xy1[1];          
        }
        plot.getGraphics().drawString(label, plot.wmap(x)-width, plot.hmap(y));
    }
    
    protected RadialPlot plot;
    ArrayList<Double> tickages;

}