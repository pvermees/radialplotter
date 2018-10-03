package RadialPlotter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public final class DensityPlot extends Plot {

    public DensityPlot(Data data) throws Exception {
        super(data);
        leftmargin = LEFTMARGIN;
        rightmargin = RIGHTMARGIN;
        topmargin = TOPMARGIN;
        bottommargin = BOTTOMMARGIN;
        symbolsize = 0.01;
        this.binomfit = new BinomFit(data);
        this.kde = new KDE();
        this.setDefault();
    }

    public final void setDefault() throws Exception{
        this.autoTimeScale(true);
        this.autoBandwidth(true);
        this.autoBinWidth(true);
        this.autoArea(true);
        this.initTimeScale();
        this.kde.reset();
        this.kde.setBandwidth(this.data,this.autoBandwidth());
        this.setBinWidth();
        this.autoUnit();
    }
    
    @Override
    public void refresh(Data data) throws Exception {
        super.refresh(data);
        minmaxt = data.getMinMaxAge();
        if (this.autoTimeScale()) {
            this.initTimeScale();
        }
        if (this.autoBinwidth()) {
            this.setBinWidth();
        }
        if (this.autoBandwidth()) {
            this.kde.reset();
        }
        if (this.autoArea()) {
            this.setArea();
        }
    }
    
    @Override
    public BinomFit getBinomFit(){
        return binomfit;
    }
    
    protected int[][] getPDFplotCoords(double[] pdf, boolean doZoom) throws Exception{
        int[][] xy = new int[2][nt+2];
        xy[0][0] = wmap(leftmargin);
        xy[0][nt+1] = wmap(1-rightmargin);
        xy[1][0] = hmap(bottommargin);
        xy[1][nt+1] = xy[1][0];
        double p;
        for (int i=0; i<nt; i++){
            p = (doZoom) ? zoom(pdf[i]) : pdf[i];
            xy[0][i+1] = wmap(leftmargin + (1-leftmargin-rightmargin)*(timescale[i]-timescale[0])/(timescale[nt-1]-timescale[0]));
            xy[1][i+1] = hmap(bottommargin + p*(1-bottommargin-topmargin));
        }
        return xy;
    }

    protected void setStyle(Color colour, int stroke, float transparency){
        g2.setColor(colour);
        g2.setStroke(new BasicStroke(stroke));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    }

    protected void resetStyle(){
        this.setStyle(Color.black,1,1f);
    }

    public void plotKDE() throws Exception {
      double[] pdf = this.rescalePDF(kde.getKDE(this.data,this.timescale,this.autoBandwidth));
      int[][] xy = getPDFplotCoords(pdf,true);
      if (this.data.preferences.doKDEfill()) {
        setStyle(this.data.preferences.KDEfillcolour(), 0, 0.5F);
        this.g2.fillPolygon(xy[0], xy[1], nt+2);
      }
      if (this.data.preferences.doKDEstroke()) {
        setStyle(this.data.preferences.KDEstrokecolour(), 2, 1.0F);
        this.g2.drawPolygon(xy[0], xy[1], nt+2);
      }
      resetStyle();
    }
  
    public void plotPDP() throws Exception {
      double[] pdf = this.rescalePDF(getPDP());
      int[][] xy = getPDFplotCoords(pdf,true);
      if (this.data.preferences.doPDPfill()) {
        setStyle(this.data.preferences.PDPfillcolour(), 0, 0.5F);
        this.g2.fillPolygon(xy[0], xy[1], nt+2);
      }
      if (this.data.preferences.doPDPstroke()) {
        setStyle(this.data.preferences.PDPstrokecolour(), 1, 1.0F);
        this.g2.drawPolygon(xy[0], xy[1], nt+2);
      }
      resetStyle();
    }

    public void plotHist() throws Exception {
        double[][] hist = this.rescaleHist();
        int numbins = hist[0].length;
        int x, y, h, w = (int)(this.width * (1.0D - this.leftmargin - this.rightmargin) * binwidth / (this.timescale[nt-1] - this.timescale[0]));
        for (int i = 0; i < numbins; i++) {
            if ((data.preferences.logarithmic() && hist[0][i] > data.log(plotmax)) ||
                (data.preferences.sqrt() && hist[0][i] > Math.sqrt(plotmax)) ||
                (data.preferences.linear() && hist[0][i] > plotmax)) break;
            x = wmap(this.leftmargin + (1.0D - this.leftmargin - this.rightmargin) * (hist[0][i] - binwidth / 2.0D - this.timescale[0]) / (this.timescale[nt-1] - this.timescale[0]));
            y = hmap(this.bottommargin + (1.0D - this.bottommargin - this.topmargin) * zoom(hist[1][i]));
            h = (int)(this.height * (1.0D - this.bottommargin - this.topmargin) * zoom(hist[1][i]));
            if (this.data.preferences.dohistfill()) {
                setStyle(this.data.preferences.histfillcolour(), 0, 0.5F);
                this.g2.fillRect(x, y, w, h);
            }
            if (this.data.preferences.dohiststroke()) {
                setStyle(this.data.preferences.histstrokecolour(), 1, 1.0F);
                this.g2.drawRect(x, y, w, h);
            }
            resetStyle();
        }
    }
  
    public void plotBells() throws Exception {
        double[][] ae = data.getDataErrArray(data.preferences.transformation());
        double[] bl = new double[ae[0].length], // belllines
                 p = new double[nt];
        int x, y1, y2;
        int[][] xy;
        for (int i=0; i<ae[0].length; i++){
            if (ae[1][i]>0){
                bl[i] = Stat.gaussianPDF(ae[0][i],ae[1][i],ae[0][i]);
            } else { // if only one column worth of data is supplied
                bl[i] = 1d;
            }
        }
        double M = Stat.maximum(bl)*100/this.data.preferences.bellheight();
        bl = this.rescale(bl,M);
        // loop through the data a second time
        for (int i=0; i<ae[0].length; i++){
            // plot bell tick:
            if (data.preferences.dotickstroke() && (ae[0][i]>=this.plotmin) && (ae[0][i]<=this.plotmax)) {
              x = getPlotX(ae[0][i]);
              y1 = hmap(bottommargin);
              y2 = hmap(bottommargin + bl[i]*(1-bottommargin-topmargin));
              this.g2.drawLine(x, y1, x, y2);
            }
            // plot bell shape
            if (data.preferences.dobellstroke()){
                // loop through time scale
                for (int j=0; j<nt; j++){
                    if (ae[1][i]>0){
                        p[j] = Stat.gaussianPDF(ae[0][i],ae[1][i],timescale[j]);
                    }
                }
                p = this.rescale(p, M);
                xy = this.getPDFplotCoords(p,false);
                this.g2.drawPolygon(xy[0], xy[1], nt+2);
            }
        }
    }

    public void plotPoints() throws Exception {
      double[][] ae = this.data.getDataErrArray("linear");
      int x, y = hmap(this.bottommargin), 
          ss = wmap(this.symbolsize);
      for (int i = 0; i < ae[0].length; i++) {
        if ((ae[0][i]>=this.plotmin) && (ae[0][i]<=this.plotmax)) {
          x = getPlotX(ae[0][i]);
          if (this.data.preferences.dopointsfill()) {
            setStyle(this.data.preferences.pointsfillcolour(), 0, 0.5F);
            this.g2.fillOval(x - ss / 2, y + ss / 2, ss, ss);
          }
          if (this.data.preferences.dopointstroke()) {
            setStyle(this.data.preferences.pointstrokecolour(), 1, 1.0F);
            this.g2.drawOval(x - ss / 2, y + ss / 2, ss, ss);
          }
          resetStyle();
        }
      }
    }
  
    public double zoom(double in){
        double out = (in-miny)/(maxy-miny);
        if (out<0d) {
            return 0d;
        } else if (out>1d) {
            return 1d;
        } else {
            return out;
        }
    }

    public double inversezoom(double out){
        double in = miny + out*(maxy-miny);
        if (in<0d) {
            return 0d;
        } else if (in>1d) {
            return 1d;
        } else {
            return in;
        }
    }

    @Override
    public void printLegend() throws Exception{
        int n = getData().count(plotmin,plotmax);        
        String samplename = getData().getSampleName(),
               legend = samplename + " (n=" + Integer.toString(n) + ")";
        int stringwidth = g2.getFontMetrics().stringWidth(legend);
        double x = (width - stringwidth)*0.5/width,
               y = data.getNumPeaks()!=0 ? 0.25*bottommargin : 1d-topmargin+this.getLineHeight();
        drawString(legend, x, y);
    }

    void plotAxes() throws Exception {
        this.plotXaxis();
        if (data.preferences.dohistfill() | data.preferences.dohiststroke()){
            this.plotYaxis();
        }
    }

    void plotXaxis() throws Exception {
        double[] xticks = getXticks();
        double xval, dt;
        String label;
        int numXticks = xticks.length, 
            length = wmap(ticklength);
        for (int i=0; i<numXticks; i++){
            xval = xticks[i];
            label = ToolBox.num2string(xval / unit, 1);
            drawTick(xval,length,label);
            if (i>0){
                if (data.preferences.linear()){
                    for (int j=1; j<minorticks; j++){
                        dt = xticks[i]-xticks[i-1];
                        xval = (xticks[i-1]+dt*j/(double)minorticks);
                        drawTick(xval,length/2);
                    }
                } else if (data.preferences.sqrt()){
                    for (int j=1; j<minorticks; j++){
                        dt = Math.sqrt(xticks[i])-Math.sqrt(xticks[i-1]);
                        xval = Math.pow((Math.sqrt(xticks[i-1])+dt*j/(double)minorticks),2);
                        drawTick(xval,length/2);
                    }
                } else {
                    for (int j=1; j<minorticks-1; j++){
                        dt = (double)j/(double)minorticks;
                        if (dt>0.1) {
                            xval = data.exp(data.log(xticks[i])+Math.log(dt));
                            xval = ToolBox.round(xval);
                            label = ToolBox.num2string(xval / unit, 1);
                            drawTick(xval,length/2,9*length/10,label);
                        }
                    }                    
                }
            }
            printXlabel();
        }
    }

    private void drawTick(double val, int length, String label) {
        drawTick(val,length,length,label);
    }

    // this is used for minor ticks
    private void drawTick(double val, int length, int offset, String label) {
        try {
            if (val<plotmin | val>plotmax){return;}
            int x = getPlotX(val),
                y = hmap(0.8*bottommargin);
            g2.drawLine(x, y, x, y + length);
            if (label != null){
                g2.drawString(label, x, y + 2*offset); // draw label
            }
        } catch (Exception e){
            // draw no tick for zero in logarithmic mode
        }
    }

    private int getPlotX(double val) throws Exception{
        double x;
        if (data.preferences.logarithmic()) {
            x = (data.log(val)-data.log(plotmin))/(data.log(plotmax)-data.log(plotmin));
        } else if (data.preferences.sqrt()) {
            x = (Math.sqrt(val)-Math.sqrt(plotmin))/(Math.sqrt(plotmax)-Math.sqrt(plotmin));
        } else {
            x = (val-plotmin)/(plotmax-plotmin);
        }
        return wmap(leftmargin + (1-leftmargin-rightmargin)*x);
    }

    // returns plot value (e.g., age) corresponding to normalized horizontal distance (0.0<x<1.0)
    private double x2val(double x) throws Exception {
        double val;
        if (x<leftmargin | x>1-rightmargin) {
            val = Data.NAN;
        } else if (data.preferences.logarithmic()) {
            val = data.exp(data.log(plotmin) + (x-leftmargin)*
                 (data.log(plotmax)-data.log(plotmin))/(1-leftmargin-rightmargin));
        } else if (data.preferences.sqrt()) {
            val = Math.pow(Math.sqrt(plotmin) + (x-leftmargin)*
                 (Math.sqrt(plotmax)-Math.sqrt(plotmin))/(1-leftmargin-rightmargin),2);
        } else {
            val = plotmin + (x-leftmargin)*
                 (plotmax-plotmin)/(1-leftmargin-rightmargin);
        }
        return val;
    }

    private void drawTick(double val, int length) throws Exception{
        this.drawTick(val, length, null);
    }

    private void printXlabel() throws Exception{
        int length = wmap(ticklength);
        String label = "";
        switch (getUnit()) {
            case Data.KA:
                label = "ka";
                break;
            case Data.MA:
                label = "Ma";
                break;
            case Data.GA:
                label = "Ga";
                break;
            default:
                break;
        }
        g2.drawString(label, wmap(1-rightmargin), hmap(0.8*bottommargin)+3*length);
    }

    double[] getXticks() throws Exception{
        int numticks;
        double[] xticks;
        double mintick, dt, magnitude;
        if (data.preferences.linear()){
            numticks = 10;
            dt = (plotmax-plotmin)/numticks; // e.g., 1.3443, 18.23, 820.32, ...
            magnitude = Math.pow(10,Math.floor(Math.log10(dt))); // e.g.,  1, 10, 100, ...
            dt = Math.round(dt/magnitude)*magnitude; // 1, 20, 800, ...
            numticks = (int) Math.ceil((plotmax-plotmin)/dt); // e.g., 10, 11, or 12
            mintick = Math.floor(plotmin/magnitude)*magnitude;
            xticks = new double[numticks+1];
            xticks[0] = mintick;
            for (int i=1; i<numticks+1; i++){
                xticks[i] = mintick + i*dt;
            }
        } else if (data.preferences.sqrt()){
            numticks = 10;
            double sq1 = Math.sqrt(plotmin);
            double sq2 = Math.sqrt(plotmax);
            dt = (sq2-sq1)/numticks;
            xticks = new double[numticks+1];
            xticks[0] = plotmin;
            for (int i=1; i<numticks+1; i++){
                xticks[i] = Math.pow(sq1+i*dt,2);
            }
        } else {
            double exp1 = Math.floor(data.log10(plotmin));
            double exp2 = Math.ceil(data.log10(plotmax));
            numticks = (int) (1+exp2-exp1);
            xticks = new double[numticks];
            for (int i=0; i<numticks; i++){
                xticks[i] = data.exp10(exp1+i);
            }
        }
        return xticks;
    }

    void plotYaxis() throws Exception {
        int x1 = wmap(leftmargin-ticklength),
            x2 = x1 - wmap(ticklength),
            x3 = wmap(leftmargin - 3.5*ticklength),
            y, th = g2.getFontMetrics().getHeight();
        double[][] hist = this.getHist();
        double normfact = data.preferences.normalise() ?
                getRawHistArea()/this.area : Stat.maximum(hist[1]);
        normfact = 1/(normfact*inversezoom(1d));
        int max = (int) Stat.maximum(hist[1]);
        if (max<10) {
            for (int i=0; i<=max; i++){
                y = hmap(bottommargin + (1-bottommargin-topmargin)*i*normfact);
                g2.drawLine(x1, y, x2, y);
                g2.drawString(Integer.toString(i), x3, y+th/4);
            }
        } else {
            int ticknum=0;
            for (int i=0; i<numYticks & ticknum<=max; i++){
                ticknum = max*i/(numYticks-1);
                y = hmap(bottommargin + (1-bottommargin-topmargin)*ticknum*normfact);
                g2.drawLine(x1, y, x2, y);
                g2.drawString(Integer.toString(ticknum), x3, y+th/4);
            }
        }
    }

    private double[][] getHist() throws Exception {
        double min = plotmin, max = plotmax;
        if (data.preferences.logarithmic()){
            min = data.log(plotmin);
            max = data.log(plotmax);
        } else if (data.preferences.sqrt()){
            min = Math.sqrt(plotmin);
            max = Math.sqrt(plotmax);            
        }
        double[][] ae = data.getDataErrArray(data.preferences.transformation());
        double[][] hist = Stat.histogramBins(ae[0], binwidth, min,max);
        return hist;
    }
    
    @Override
    public void plotMinAgePeak() throws Exception {
        MinAgeMod minagemod = binomfit.getMinAgeMod();
        double[] minterr = minagemod.getMinAgeErr();
        if (minterr[0]>=plotmin & minterr[0]<=plotmax){
            drawLine(minterr[0]);
            this.plotPeakLabels(minterr[0], minterr[1]);
        }
    }

    @Override
    public void drawLine(double t) throws Exception {
        int x  = getPlotX(t),
            y1 = hmap(1-this.topmargin),
            y2 = hmap(this.bottommargin);
        g2.drawLine(x, y1, x, y2);
    }
    
    @Override
    public void plotBinomPeaks(int numpeaks) throws Exception {
        double p, perr;
        double[] ae;
        double[][] pi = binomfit.getPi();
        int x;
        for (int i=0; i<numpeaks; i++){
            ae = binomfit.getPeakAgeErr(i);
            if (ae[0]>=plotmin & ae[0]<=plotmax){
                drawLine(ae[0]);
                p = 100*pi[numpeaks-1][i];
                perr = binomfit.getPerr(i,numpeaks);
                plotPeakLabels(ae[0],ae[1],p,perr);
            }
        }
    }

    public void plotPeakLabels(double t, double terr) throws Exception {
        this.plotPeakLabels(t, terr, Data.NAN, Data.NAN);
    }

    public void plotPeakLabels(double t, double terr, double p, double perr) throws Exception{
        int x = getPlotX(t),
            y = hmap(1-topmargin/5),
            th = g2.getFontMetrics().getHeight();
        String[] labels = ToolBox.num2string(t/getUnit(), terr/getUnit(), 1);
        String tlabel = labels[0] + "±" + labels[1];
        g2.rotate(Math.PI/2.0,x,y);
        g2.drawString(tlabel,x,y-th/3);
        if (p != Data.NAN){
            labels = ToolBox.num2string(p, perr, 1);
            String plabel = "(" + labels[0] + "±" + labels[1] + "%)";
            g2.drawString(plabel,x,y+2*th/3);
        }
        g2.rotate(-Math.PI/2.0,x,y);
    }

    public void initTimeScale() throws Exception {
        minmaxt = data.getMinMaxAge();
        double dt = minmaxt[1]-minmaxt[0], thestart, theend;
        double n = 0.2, m = 0d, k = 1.2;
        if (data.preferences.logarithmic()){
            n = 0.5; m = 0.5; k = 2d;
        }
        if (dt<minmaxt[0] || minmaxt[0]<0){
            thestart = minmaxt[0]-(minmaxt[1]-minmaxt[0])*n;
            theend = minmaxt[1]+(minmaxt[1]-minmaxt[0])*n;
        } else {
            thestart = minmaxt[0]*m;
            theend = minmaxt[1]*k;
        }
        this.initTimeScale(thestart, theend);
    }

    @Override
    public void initTimeScale(double mint, double maxt) throws Exception {
        plotmin = mint;
        plotmax = maxt;
        this.timescale = DensityPlot.getTimeScale(data, plotmin, plotmax, nt);
        this.setMinorTicks(plotmin,plotmax);
    }
    
    static double[] getTimeScale(Data data, double mint, double maxt, int nt) throws Exception {
        double[] timescale = new double[nt];
        double dt;
        if (data.preferences.logarithmic()){
            dt = (data.log(maxt)-data.log(mint))/(nt-1);
        } else if (data.preferences.sqrt()){
            dt = (Math.sqrt(maxt)-Math.sqrt(mint))/(nt-1);
        } else {
            dt = (maxt-mint)/(nt-1);
        }
        for (int i=0; i<nt; i++){
            if (data.preferences.logarithmic()){
                timescale[i] = data.log(mint) + i*dt;
            } else if (data.preferences.sqrt()){
                timescale[i] = Math.sqrt(mint) + i*dt;
            } else {
                timescale[i] = mint + i*dt;
            }
        }
        return timescale;
    }
    
    protected void setMinorTicks(double mintick, double maxtick) throws Exception {
        if (data.preferences.logarithmic()){
            this.minorticks = 10;
            double range = data.log10(maxtick)-data.log10(mintick);
            this.minorticks /= range;
            int om = (int) Math.floor(data.log10(minorticks)); // order of magnitude of number of ticks
            double mt = minorticks/data.exp10(om); // rescale to between 0 and 10
            if (mt>0d && mt<=1){
                mt = 1;
            } else if (mt>1 && mt<=2){
                mt = 2;
            } else if (mt>2 && mt<=7.5){
                mt = 5;
            } else if (minorticks>5 && minorticks<=10){
                mt = 10;
            }
            minorticks = (int) (mt*data.exp10(om)); // scale back to original order of magnitude
        } else {
            this.minorticks = 5;
        }
    }  
    
    public void setNumMinorTicks(int n) {
        this.minorticks = n;
    }    

    protected double getKDEbandwidth() throws Exception {
        return this.kde.getBandwidth();
    }
    
    public void checkBinWidth(String foo) throws Exception {
        String bar = ToolBox.num2string(this.getBinWidth(),2);
        double bwa;
        if (!bar.equals(foo) & ToolBox.isNumeric(foo)){
            this.autoBinWidth = false;
            bwa = Double.valueOf(foo);
        } else {
            bwa = this.getBinWidth();
        }
        if (!this.data.preferences.logarithmic() & bwa>0.0){
            this.setBinWidth(bwa*this.unit);
        } else if (bwa>0.0) { // logarithmic or arcsin
            this.setBinWidth(bwa);
        } else {
            this.autoBinWidth = true; // if the binwidth is invalid, switch to auto
        }
    }
    
    public void setArea(double area) throws Exception {
        this.area = area;
    }
    
    public void setArea() throws Exception {
        this.area = Data.NAN;
        double[] dens = kde.getKDE(this.data,this.timescale,this.autoBandwidth);
        this.setArea(this.getRawPDFarea(dens)/Stat.maximum(dens));
    }
    
    public double getArea() throws Exception {
        return this.area;
    }

    private double getRawHistArea() throws Exception {
        double[][] hist = this.getHist();
        double dthist;
        if (data.preferences.linear()) {
            dthist = this.binwidth/(this.plotmax-this.plotmin);
        } else { // binwidth is alread expressed in logarithmic form
            dthist= this.binwidth/(data.log(this.plotmax)-data.log(this.plotmin));
        }
        return Stat.arraySum(hist[1])*dthist;
    }
    
    private double getRawPDFarea(double[] pdf) throws Exception {
        int n = 0;
        double oppervlakte = 0d, m = plotmin, M = plotmax;
        if (data.preferences.logarithmic()){
            m = data.log(plotmin);
            M = data.log(plotmax);
        } else if (data.preferences.logarithmic()){
            m = Math.sqrt(plotmin);
            M = Math.sqrt(plotmax);            
        }
        for (int i=0; i<timescale.length; i++){
            if (timescale[i]>=m && timescale[i]<=M){
                oppervlakte += pdf[i];
                n++;
            }
        }
        return oppervlakte/n;
    }   
    
    public double[] getPDP() throws Exception {
        double[][] ae = data.getDataErrArray(data.preferences.transformation());
        double[] pdp = new double[nt];
        for (int i=0; i<ae[0].length; i++){
            for (int j=0; j<nt; j++){
                pdp[j] += Stat.gaussianPDF(ae[0][i], ae[1][i], timescale[j]);
            }
        }
        return pdp;
    }

    public double[] getBells() throws Exception {
        double[][] ae = data.getDataErrArray(data.preferences.transformation());
        double[] pdp = new double[nt];
        double p;
        for (int i=0; i<ae[0].length; i++){
            for (int j=0; j<nt; j++){
                p = Stat.gaussianPDF(ae[0][i], ae[1][i], timescale[j]);
                if (p>pdp[j]) {pdp[j] = p;}
            }
        }
        return this.rescale(pdp,Stat.maximum(pdp)*10);
    }
    
    protected double[] rescalePDF(double[] pdf) throws Exception{
        double normfact = data.preferences.normalise() ?
                this.getRawPDFarea(pdf)/this.area : Stat.maximum(pdf);
        return this.rescale(pdf,normfact);
    }   

    private double[][] rescaleHist() throws Exception{
        double[][] hist = getHist();
        double normfact = data.preferences.normalise() ?
                getRawHistArea()/this.area : Stat.maximum(hist[1]);
        hist[1] = this.rescale(hist[1],normfact);
        return hist;
    }     
    
    private double[] rescale(double[] dens, double norm) throws Exception {
        int n = dens.length;
        double[] out = new double[dens.length];
        for (int i=0; i<n; i++){
            out[i] = dens[i]/norm;
        }
        return out;
    }
    
    public boolean autoTimeScale(){
        return !this.fixedaxes;
    }

    public void autoBandwidth(boolean auto){
        this.autoBandwidth = auto;
    }

    public void autoBinWidth(boolean auto){
        this.autoBinWidth = auto;
    }

    public void autoArea(boolean auto) throws Exception{
        this.autoArea = auto;
        data.preferences.normalise(!auto);
    }
    
    public boolean autoBandwidth(){
        return this.autoBandwidth;
    }
    
    public boolean autoBinwidth(){
        return this.autoBinWidth;
    }
    
    public boolean autoArea(){
        return this.autoArea;
    }

    public double getMin(){
        return plotmin;
    }

    public double getMax(){
        return plotmax;
    }

    // set default bin width
    public void setBinWidth() throws Exception {
        double bw;
        if (data.preferences.normalise()){
            double multiplier = data.preferences.epanechnikov() ? 1.3333 : 2.5066;
            this.kde.setBandwidth(data, this.autoBandwidth);
            bw = this.kde.getBandwidth()*multiplier;
        } else {
            int numbins = (int)Math.ceil(Math.sqrt(data.length()));
            if (data.preferences.logarithmic()) {
                bw = (data.log(minmaxt[1])-data.log(minmaxt[0]))/numbins;
                numbins = (int) Math.ceil((data.log(plotmax)-data.log(plotmin))/bw);
                bw = (data.log(plotmax)-data.log(plotmin))/numbins;
            } else if (data.preferences.sqrt()) {
                bw = (Math.sqrt(minmaxt[1])-Math.sqrt(minmaxt[0]))/numbins;
                numbins = (int) Math.ceil((Math.sqrt(plotmax)-Math.sqrt(plotmin))/bw);
                bw = (Math.sqrt(plotmax)-Math.sqrt(plotmin))/numbins;
            } else {
                bw = (minmaxt[1]-minmaxt[0])/numbins;
                numbins = (int) Math.ceil((plotmax-plotmin)/bw);
                bw = (plotmax-plotmin)/numbins;
            }
        }
        setBinWidth(bw);
    }

    public void setBinWidth(double binwidth) throws Exception {
        this.binwidth = binwidth;
    }

    public double getBinWidth() throws Exception {
        return binwidth;
    }  
    
    public void setMinY(int y){
        this.miny = (double)y;
    }

    public void setMaxY(double y) throws Exception {
        double rely = (1d-bottommargin-y/height)/(1d-bottommargin-topmargin);
        if (rely>1d){
            this.maxy = 1d;
        } else if (rely<0d) {
            // do nothing
        } else {
            this.maxy = inversezoom(rely);
        }
    }

    @Override
    public void PopUp(MouseEvent e) throws Exception {
        DecimalFormat df = new DecimalFormat("0.00");
        final JPopupMenu pop = new JPopupMenu();
        double val = this.x2val(this.winvmap(e.getX()));
        pop.add(new JMenuItem("value = " + df.format(val/this.unit)));
        pop.show(e.getComponent(),e.getX(),e.getY());
    }
    
    @Override
    double[][] getPlotCoords() throws Exception {
        double[] pdf = kde.getKDE(this.data,this.timescale,this.autoBandwidth);
        double[][] xy = new double[pdf.length][2];
        for (int i=0; i<pdf.length; i++){
            xy[i][0] = timescale[i];
            xy[i][1] = pdf[i];
        }
        return xy;
    }

    private double[] timescale;
    static int nt = 512; // length of the timescale array (must be power of 2!)
    final double LEFTMARGIN = 0.15, RIGHTMARGIN = 0.1,
                 BOTTOMMARGIN = 0.2, TOPMARGIN = 0.2;
    protected int numYticks = 5, minorticks = 5;
    protected boolean autoTimeScale = true, autoBinWidth = true, autoArea = true;
    protected double miny=0d, maxy=1d, binwidth=-1.0, area=-1.0;

}
