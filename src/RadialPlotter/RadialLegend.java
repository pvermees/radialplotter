package RadialPlotter;

import java.text.DecimalFormat;

public class RadialLegend {
    
    RadialLegend(RadialPlot plot) throws Exception {
        this.plot = plot;
    }
    
    public void printLegend() throws Exception {
        this.drawTitle(1);
        this.drawCentralAge(2);
        if (plot.getData() instanceof FTdata){
            this.drawX2(4);
        }
        this.drawPeaks(16);
    }

    private void drawTitle(int lineno) throws Exception {
        int n = plot.getData().length();
        String samplename = plot.getData().getSampleName(),
               legend = samplename + " (n=" + Integer.toString(n) + ")";
        plot.drawString(legend, plot.getLeftMargin(), 1-lineno*plot.getLineHeight());
    }
    
    private void drawPeaks(int lineno) throws Exception {
        BinomFit peaks = plot.getBinomFit();
        String unit = "";
        double t, terr;
        if (plot.getData() instanceof OtherData){
            unit = "";
        } else if (plot.unit == Data.MA){
            unit = "Ma";
        } else if (plot.getUnit() == Data.KA){
            unit = "ka";
        } else if (plot.getUnit() == Data.A){
            unit = "yr";
        }
        if (peaks.getNumPeaks()<0){
            t = peaks.getMinAgeMod().gspbest[0];
            terr = peaks.getMinAgeMod().gammaErr;
            drawMinAgePeak(lineno, t, terr, unit);
        } else {
            drawBinomPeaks(lineno, peaks, unit);
        }
    }
    
    private void drawMinAgePeak(int lineno, double t, double terr, String unit) throws Exception {
        this.drawLabel(t, terr, Data.NAN, Data.NAN, lineno, 1, 3, unit);
    }

    private void drawBinomPeaks(int lineno, BinomFit peaks, String unit) throws Exception{
        double[][] pi = peaks.getPi(),
                   theta = peaks.getTheta();
        double[] ae;
        double t, terr, p, perr;
        int k = peaks.getNumPeaks();
        for (int i = 0; i<k; i++){
            ae = peaks.getPeakAgeErr(i);
            t = ae[0];
            terr = ae[1];
            p = 100*pi[k-1][i];
            perr = peaks.getPerr(i, k);
            drawLabel(t,terr,p,perr,lineno,i,k,unit);
        }
    }

    private void drawLabel(double t, double terr, double p, double perr, int lineno, int i, int k, String unitlabel) throws Exception {
        String[] tlabels;
        tlabels = ToolBox.num2string(t/plot.unit, terr/plot.unit, precision);
        String text = tlabels[0] + "±" + tlabels[1] + unitlabel;
        if (p==Data.NAN){
            text = "Minimum age: " + text;
        } else {
            String[] plabels = ToolBox.num2string(p, perr, precision);
            text = "Peak " + Integer.toString(i+1) + ": " + text +
                   " (" + plabels[0] + "±" + plabels[1] + "%)";
        }
        plot.drawString(text,plot.getLeftMargin()+0.01, 1-(lineno-k+i)*plot.getLineHeight());
    }
    
    private void drawCentralAge(int lineno) throws Exception {
        String unitlabel = "", message1, message2 = "Dispersion = ";
        double[] tsd;
        Data data = plot.getData();
        if (data instanceof OtherData){
            unitlabel = "";
        } else if (plot.unit == Data.MA) {
            unitlabel = "Ma";
        } else if (plot.unit == Data.KA) {
            unitlabel = "ka";
        } else if (plot.unit == Data.A) {
            unitlabel = "yr";
        }
        try {
            tsd = plot.data.getCentralAge();
            message1 = (data instanceof OtherData) ? "Central value = " : "Central age = ";
        } catch (ArithmeticException e){
            tsd = plot.getArithmeticMean();
            message1 = (data instanceof OtherData) ? "Mean value = " : "Mean age = ";
        }
        String[] labels = ToolBox.num2string(tsd[0]/plot.unit, tsd[1]/plot.unit, precision);
        message1 += labels[0] + " ± " + labels[1] + " " + unitlabel + " (1σ)";
        message2 += ToolBox.num2string(tsd[2]*100,precision) + " %";
        if (plot.data.offset>0){
            message1 = "[" + message1 + "]";
            message2 = "[" + message2 + "]";
        }
        plot.drawString(message1, plot.getLeftMargin(), 1-lineno*plot.getLineHeight());
        plot.drawString(message2, plot.getLeftMargin(), 1-(lineno+1)*plot.getLineHeight());
    }
    
    private void drawX2(int lineno) throws Exception {
        DecimalFormat fm = new DecimalFormat("0.00");
        String label = "P(χ²) = " + fm.format(FT.getPX2((FTdata)(plot.getData())));
        plot.drawString(label, plot.getLeftMargin(), 1-lineno*plot.getLineHeight());
    }
    
    protected RadialPlot plot;
    protected int precision = 1;
}
