package RadialPlotter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;

public abstract class Plot {

    public Plot(Data data) throws Exception {
        this.data = data;
        this.minmaxt = data.getMinMaxAge();
        this.markers = "";
    }
    
    abstract void plotMinAgePeak() throws Exception;

    abstract void plotBinomPeaks(int numpeaks) throws Exception;
    
    abstract void drawLine(double t) throws Exception;
    
    public void plotMarkers(String markers) throws Exception {
        this.markers = markers;
        this.plotMarkers();
    }
    
    public void plotMarkers() throws Exception {
        if (this.markers.isEmpty()) return;
        String[] ticks = this.markers.split("[,;\\-\\_:+\\s]");
        int nt = ticks.length;
        double tick;
        for (int i=0; i<nt; i++){
            try {
                tick = Double.parseDouble(ticks[i]);
                drawLine(tick*this.unit);
            } catch (Exception e){}
        }
    }

    abstract void printLegend() throws Exception;

    abstract BinomFit getBinomFit() throws Exception;

    abstract void PopUp(MouseEvent e) throws Exception;

    abstract void initTimeScale(double min, double max) throws Exception;
    
    abstract double[][] getPlotCoords() throws Exception;    

    public void printStackTrace(Exception e) {
        try {
            drawString(e.getMessage(), leftmargin + 0.01, 1 - 1 * lineheight);
        } catch (Exception ex) {
            if (Data.DEBUGMODE){ex.printStackTrace(System.out);}
        }
    }

    public double getLeftMargin(){
        return leftmargin;
    }

    public double getRightMargin(){
        return rightmargin;
    }
    
    public boolean firstRun() throws Exception {
        return (minmaxt[0]==Data.MINT && minmaxt[1]==Data.MAXT);
    }

    /* @param text = a text string
     * @param x = x-coordinate between 0 and 1, counting from left to right
     * @param y = y-coordinate between 0 and 1, counting from BOTTOM to TOP!
     */
    public void drawString(String text, double x, double y) throws Exception {
        g2.drawString(text, wmap(x), hmap(y));
    }

    public void plotPeaks() throws Exception {
        int numpeaks = data.getNumPeaks();
        if (numpeaks<0){
            plotMinAgePeak();
        } else {
            plotBinomPeaks(numpeaks);
        }
    }

    public void refresh(Data data) throws Exception {
        this.data = data;
        this.setMinMaxAge();
        if (data instanceof OtherData | !fixunits) {
            autoUnit();
        }
        this.setOffset();
        this.binomfit = new BinomFit(data);
        kde.setBandwidth(data, this.autoBandwidth);
    }
    
    public void setOffset() throws Exception{
        minmaxt = this.getMinMaxAge();
        double min = plotmin<minmaxt[0] ? plotmin : minmaxt[0];
        if (min>0){
            data.offset = 0;
        } else if (min==0){
            data.offset = 1;
        } else {
            data.offset = Math.pow(10, Math.ceil(ToolBox.log10(-min)))+1;
        }
    }
    
    public void setGraphics(Graphics2D graphics, int width, int height) throws Exception {
        g2 = graphics;
        this.width = (double) width;
        this.height = (double) height;
    }

    public Graphics2D getGraphics(){
        return g2;
    }

    public void setData(Data data){
        this.data = data;
    }

    public Data getData(){
        return data;
    }

    // map val from a [0,1] to an integer number of horizontal pixels
    public int wmap(double val) throws Exception {
        return (int)(width*val);
    }

    public double winvmap(int val) throws Exception {
        return (double)val/width;
    }

    // map val from a [0,1] to an integer number of vertical pixels
    // and flip the coordinates so that they go from bottom to top
    public int hmap(double val) throws Exception {
        return (int)(height*(1-val));
    }

    public double hinvmap(int val) throws Exception {
        return 1- (double)val/height;
    }

    public double getWidth(){
        return this.width;
    }

    public double getHeight(){
        return this.height;
    }

    public void autoTimeScale(boolean auto){
        this.fixedaxes = !auto;
    }    
    
    public void setMinMaxAge() throws Exception {
        if (!this.fixedaxes){
            minmaxt = data.getMinMaxAge();
            plotmin = minmaxt[0];
            plotmax = minmaxt[1];
        }
    }    

    public void setMinMaxAge(double minAge, double maxAge) throws Exception {
        plotmin = minAge;
        plotmax = maxAge;
    }

    public double[] getMinMaxAge() throws Exception {
        double[] out = {plotmin, plotmax};
        return out;
    }

    public void setCentralAge(double centralt) throws Exception {
        this.centralt = centralt;
    }
    
    public double getCentralAge() throws Exception {
        if (!this.fixedaxes){
            centralt = data.z2t(data.get_z0());
        }
        return this.centralt;
    }
    
    public double getLineHeight(){
        return this.lineheight;
    }

    public void setLineHeight(double lineheight){
        this.lineheight = lineheight;
    }

    public int getUnit() throws Exception {
        return this.unit;
    }
    
    public void fixUnits(boolean fixit){
        this.fixunits = fixit;
    }
    
    public void autoUnit() throws Exception{
        this.fixUnits(false);
        double range = plotmax - plotmin;
        if (data instanceof OtherData){
            this.setUnit(Data.A);
        } else if (range<2*Data.A){
            this.setUnit(Data.A);
        } else if (range<2*Data.MA){
            this.setUnit(Data.KA);
        } else {
            this.setUnit(Data.MA);
        }
    }
    
    public void setUnit(int unit) throws Exception {
        this.unit = unit;
    }
    
    public void plot2csv(String filepath) throws Exception {
        String nl = System.getProperties().getProperty("line.separator");
        BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
        out.write(data.samplename + nl);
        double[][] xys = getPlotCoords();
        for (double[] xy : xys) {
            out.write(xy[0] + "," + xy[1] + nl);
        }
        out.close();
    }
    
    public String getBandWidth() throws Exception {
        double bw = this.kde.getBandwidth();
        if (bw<=0){
            return "";
        } else if (this.data.preferences.linear()) {
            return ToolBox.num2string(bw/this.unit,2);
        } else { // logarithmic or arcsin
            return ToolBox.num2string(bw,2);
        }
    }
    
    // this function gets called by RadialOptions and DensityOptions
    public void checkBandWidth(String foo) throws Exception{
        String bar = ToolBox.num2string(this.kde.getBandwidth(),2);
        double bwa;
        if (!bar.equals(foo) & ToolBox.isNumeric(foo)){
            this.autoBandwidth = false;
            bwa = Double.valueOf(foo);
        } else {
            bwa = this.kde.getBandwidth();
        }
        if (this.data.preferences.linear() & bwa>0.0){
            this.kde.setBandwidth(bwa*this.unit);
        } else if (bwa>0.0){ // logarithmic or arcsin
            this.kde.setBandwidth(bwa);
        } else {
            this.autoBandwidth = true; // if the bandwidth is invalid, switch to auto
        }        
    }
    
    protected Data data;
    protected Graphics2D g2;
    protected double width, height, centralt; // width and height of parent JPanel
    protected double[] minmaxt;
    protected boolean fixedaxes = false, fixunits = false, autoBandwidth = true;
    protected int unit = Data.A;     
    protected double leftmargin, rightmargin, bottommargin, topmargin,
                     symbolsize = 0.015, ticklength = 0.02, lineheight=0.05,
                     plotmin, plotmax;
    protected String markers;
    BinomFit binomfit;
    KDE kde;

}
