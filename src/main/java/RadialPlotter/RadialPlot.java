package RadialPlotter;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class RadialPlot extends Plot {

    public RadialPlot(Data data) throws Exception {
        super(data);
        leftmargin = LEFTMARGIN;
        rightmargin = RIGHTMARGIN;
        topmargin = TOPMARGIN;
        bottommargin = BOTTOMMARGIN;
        maxrx = 0;
        minry = 0;
        maxry = 0;
        r0 = 1 - leftmargin - rightmargin/2;
        this.radialscale = new RadialScale(this);
        this.radialaxes = new RadialAxes(this);
        this.colourscale = new ColourScale(this);
        this.binomfit = new BinomFit(data);
        this.radiallegend = new RadialLegend(this);
        this.sigmalines = new SigmaLines(this);
        kde = new KDE();
    }
    
    @Override
    public void refresh(Data data) throws Exception {
        super.refresh(data);
        if (this.fixedaxes){
            this.data.set_z0(this.data.t2z(this.centralt));
        }
    }

    public void plotData() throws Exception {
        data2rxry(); // refresh the x-y coordinates of the data set
        colourscale.setMinMaxC();
        double c;
        double[] xy;
        for (int i=0;i<data.getRadialX().size();i++){
            xy = rxry2xy(data.getRadialX().get(i), data.getRadialY().get(i));
            // plot data symbol
            c = data.getC(i);
            if (c == Data.NAN){
                g2.setColor(Color.white);
            } else {
                g2.setColor(colourscale.getColour(c));
            }
            g2.fillOval(wmap(xy[0]-symbolsize/2), hmap(xy[1])-wmap(symbolsize/2), 
                        wmap(symbolsize), wmap(symbolsize));
            g2.setColor(Color.black);
            g2.drawOval(wmap(xy[0]-symbolsize/2), hmap(xy[1])-wmap(symbolsize/2), 
                        wmap(symbolsize), wmap(symbolsize));
            if (data.preferences.datalabels()){
                int rownumber = data.getRowNumber(i);
                g2.drawString(Integer.toString(rownumber+1), wmap(xy[0]), hmap(xy[1]));
            }
        }
    }

    // this time scale is only used for the Abanico plot
    static double[] getTimeScale(Data data, double mint, double maxt, int nt) throws Exception {
        double[] timescale = new double[nt];
        double M = data.t2z(maxt),
               m = data.t2z(mint),
               dt = (M - m)/(nt - 1);
        for (int i=0; i<nt; i++){
            timescale[i] = m + i*dt;
        }
        return timescale;
    }
    
    public void plotKDE() throws Exception {
        Preferences prefs = this.data.preferences;
        double[] timescale = RadialPlot.getTimeScale(this.data,this.plotmin,this.plotmax,DensityPlot.nt);
        int[] x = new int[timescale.length], y = new int[timescale.length];
        double[] xy, z = ToolBox.convertDoubles(data.z),
                 pdf = kde.getKDE(z, timescale, this.autoBandwidth, prefs.adaptive(),prefs.epanechnikov());
        double pdfmax = ToolBox.getMax(pdf);
        for (int i=0; i<timescale.length; i++){
            xy = this.rz2xy(this.r0*(1+0.5*this.RIGHTMARGIN*pdf[i]/pdfmax), timescale[i]);
            x[i] = wmap(xy[0]);
            y[i] = hmap(xy[1]);
        }
        this.g2.drawPolyline(x, y, DensityPlot.nt);
    }
    
    public void resetKDE() throws Exception {
            this.autoBandwidth = true;
            this.kde.setBandwidth(data, true);        
    }

    public void plotSigmaLines() throws Exception {
        sigmalines.plot();
    }

    public void plotRadialScale() throws Exception {
        radialscale.plot();
    }
    
    @Override
    public void printLegend() throws Exception {
        radiallegend.printLegend();
    }

    @Override
    public void plotMinAgePeak() throws Exception {
        MinAgeMod minagemod = binomfit.getMinAgeMod();
        double t = minagemod.getMinAgeErr()[0];
        this.drawLine(t);
    }

    @Override
    public void drawLine(double t) throws Exception {
        double[] xy1 = rxry2xy(0, 0),
                 xy2 = rz2xy(r0, t2z(t));
        g2.drawLine(wmap(xy1[0]), hmap(xy1[1]), wmap(xy2[0]), hmap(xy2[1]));
    }
    
    @Override
    public void plotBinomPeaks(int numpeaks) throws Exception {
        double t, beta;
        double[][] theta = binomfit.getTheta();
        for (int i=0; i<numpeaks; i++){
            if (data instanceof FTdata){
                beta = ToolBox.log(theta[numpeaks-1][i]/(1-theta[numpeaks-1][i]));
                t = FT.getFTage(((FTdata)data).getZeta(),
                                ((FTdata)data).getRhoD(), Math.exp(beta), 1);
            } else {
                t = data.exp(theta[numpeaks-1][i]);
            }
            drawLine(t);
        }
    }
    
    public void plotAxes() throws Exception {
        radialaxes.plot();
    }

    public void plotColourScale() throws Exception {
        colourscale.plot();
    }
    
    public ColourScale getColourScale() throws Exception {
        return this.colourscale;
    }   

   /* rebuilds the ArrayList of x,y coordinates for the radial plot
    * this function must be run before getRadialX or getRadialY
    */
    public void data2rxry() throws Exception {
        data.data2rxry(fixedaxes);
        // set bounding box variables
        if (data.radialX.size()>1){
            this.setBoundingBox(data.get_z());
        }
    }
    
    private void setBoundingBox(ArrayList<Double> z) throws Exception {
        maxrx = ToolBox.getMax(data.getRadialX());
        double[] minmaxage = this.getMinMaxAge();
        double minz = this.t2z(minmaxage[0]);
        double maxz = this.t2z(minmaxage[1]);
        minry = (minz - data.get_z0())*maxrx; // rule of three
        maxry = (maxz - data.get_z0())*maxrx; // rule of three
    }
    
    public double t2z(double t) throws Exception {
        return data.t2z(t);
    }
    
    /* converts x,y coordinates from Galbraith's scale
     * to the map scale ([0 1]), counting from lower left and including margins
     */ 
    public double[] rxry2xy(double rx, double ry) throws Exception {
        double[] xy = new double[2];
        xy[0] = rx/maxrx; // scale to [0,1]
        xy[0] = leftmargin + xy[0]*(1.0d-leftmargin-rightmargin); // add margins
        // make sure that topmargin <= y <= 1-bottommargin
        xy[1] = (ry-minry)/(maxry-minry); // scale to [0,1]
        xy[1] = bottommargin + xy[1]*(1.0d-topmargin-bottommargin); // add margins
        return xy;
    }
    
    public double[] xy2rxry(double x, double y) throws Exception {
        double[] rxry = new double[2];
        x = (x - leftmargin)/(1.0d-leftmargin-rightmargin);
        rxry[0] = x * maxrx;
        // make sure that topmargin <= y <= 1-bottommargin
        y = (y - bottommargin)/(1.0d-topmargin-bottommargin); // add margins
        rxry[1] = minry + y*(maxry-minry); // scale to [0,1]
        return rxry;
    }
  
    /** rz2xy calculates x,y coordinate of a point specified by a 
     * Data dataset, a z-value, and a radial distance r
     * this function is used for drawing the radial scale, including tick marks
     * 
     * @param r = radial distance
     * @param z = Galbraith's radial parameter, i.e. a slope
     * @return returns a 1x2 array with the x and y coordinate
     * @throws java.lang.Exception
     */ 
    public double[] rz2xy(double r, double z) throws Exception {
        double a = r, b = a*(width/height);
        double slope = (z-data.get_z0())*(maxrx/(maxry-minry))*
                (1-bottommargin-topmargin)/(1-leftmargin-rightmargin);
        double[] origin = getOrigin();
        double x = 1/Math.sqrt(1/(a*a) + slope*slope/(b*b));
        double y = slope*x;
        double[] xy = {origin[0] + x, origin[1] + y}; // translate center of circle
        return xy;
    }
    
    /** this function returns the x-y coordinates (accounting for margins)
     * corresponding to Galbraith's x=0 , y=0 (i.e., at the central value z0)
     * you should run data2rxry(option) prior to running this function
     * @return 
     * @throws java.lang.Exception
     */
    public double[] getOrigin() throws Exception {
        return(rxry2xy(0, 0));
    }

    public double[] getMinMaxC() throws Exception {
        return data.getMinMaxC();
    }  
    
    public void setSymbolSize(double size){
        this.symbolsize = size;
    }
    
    public double get_r0(){
        return this.r0;
    }

/**
 * Returns a vector of integer x values to be used as input to a radial plot.
 * you must run data2rxry(option) before running this function 
     * @return 
     * @throws java.lang.Exception
 */ 
    public ArrayList<Double> getRadialX() throws Exception {
        return data.getRadialX();
    }
    
/**
 * Returns a vector of integer y values to be used as input to a radial plot.
 * you must run data2rxry(option) before running this function
     * @return 
     * @throws java.lang.Exception
 */ 
    public ArrayList<Double> getRadialY() throws Exception {
        return data.getRadialY();
    }    

/* Returns the central value used for the radial plot.
 * you must run data2rxry(option) before running this function
 */ 
    public double get_z0() throws Exception {
        return data.get_z0();
    }
    
    // with this function, you can change the central value of a radial plot
    public void set_z0(double z0) throws Exception {
        data.set_z0(z0);
    }
    
    public double getTicklength(){
        return ticklength;
    }
    
    public void setRightMargin(int x) throws Exception {
        double rm = (width-x)/width;
        double[] origin = getOrigin();
        if (rm < RIGHTMARGIN){
            rightmargin = RIGHTMARGIN;
        } else if (1-rm < leftmargin){
            rightmargin = 1.01-leftmargin;
        } else {
            rightmargin = rm;
        }
        // if you change the rightmargin, you should also change the
        // topmargin and bottommargins correspondingly
        bottommargin = BOTTOMMARGIN + (origin[1]-BOTTOMMARGIN)*
                       (rightmargin-RIGHTMARGIN)/(1-RIGHTMARGIN-origin[0]);
        topmargin = TOPMARGIN + (1-TOPMARGIN-origin[1])*
                       (rightmargin-RIGHTMARGIN)/(1-RIGHTMARGIN-origin[0]);
    }
    
    public void insertEntry(double X, double Y, double Z, int r) throws Exception {
        data.insertEntry(X, Y, Z, r);
        data2rxry();
    }
    
    public void removeEntry(int r) throws Exception {
        data.removeEntry(r);
        data2rxry();
    }
    
    // returns the font size as a fraction of the window height
    public double getFontSize() throws Exception {
        double pointsize = (double)(g2.getFont().getSize());
        return (pointsize/this.height);
    }
    
    public RadialScale getRadialScale() throws Exception {
        return this.radialscale;
    }

    double[] getArithmeticMean() throws Exception {
        return data.getArithmeticMean();
    }

    // allow minmaxt and z0 to be set by the user instead of being automaticlly calculated
    public void fixAxes(boolean fix) throws Exception {
        this.fixedaxes = fix;
    }

    @Override
    public void initTimeScale(double min, double max) throws Exception {
        this.fixAxes(true);
        this.setCentralAge(data.getCentralAge()[0]);
        this.setMinMaxAge(min, max);
    }
    
    public boolean axesFixed() throws Exception {
        return this.fixedaxes;
    }
    
    @Override
    public BinomFit getBinomFit() throws Exception {
        return binomfit;
    }

    @Override
    public void PopUp(MouseEvent e) throws Exception {
        DecimalFormat df = new DecimalFormat("0.00");
        double x = this.winvmap(e.getX());
        double y = this.hinvmap(e.getY());
        double rxry[] = this.xy2rxry(x,y);
        double zs[] = data.rxry2zs(rxry[0], rxry[1]);
        double ts[] = data.zs2ts(zs[0], zs[1]);
        String msg = "age = ";
        String unitlabel = "";
        if (data instanceof OtherData){
            unitlabel = "";
            msg = "value = ";
        } else if (unit == Data.MA) {
            unitlabel = "Ma";
        } else if (unit == Data.KA) {
            unitlabel = "ka";
        } else if (unit == Data.A) {
            unitlabel = "yr";
        }
        final JPopupMenu pop = new JPopupMenu();
        pop.add(new JMenuItem(msg + df.format(ts[0]/unit) + unitlabel));
        pop.add(new JMenuItem("err = " + df.format(ts[1]/unit) + unitlabel));
        pop.show(e.getComponent(),e.getX(),e.getY());
    }
    
    @Override
    double[][] getPlotCoords() throws Exception {
        ArrayList<Double> X = data.getRadialX(),
                          Y = data.getRadialY();
        double[][] xy = new double[X.size()][2];
        for (int i=0; i<X.size(); i++){
            xy[i][0] = X.get(i);
            xy[i][1] = Y.get(i);
        }
        return xy;
    }      

    final double LEFTMARGIN = 0.1, RIGHTMARGIN = 0.25,
                 BOTTOMMARGIN = 0.3, TOPMARGIN = 0.15, 
                 ABANICORIGHTMARGIN = 0.3;
    protected double maxrx, minry, maxry, r0;
    protected RadialScale radialscale;
    protected RadialLegend radiallegend;
    protected RadialAxes radialaxes;
    protected ColourScale colourscale;
    protected SigmaLines sigmalines;

}