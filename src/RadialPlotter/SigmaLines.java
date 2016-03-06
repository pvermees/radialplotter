package RadialPlotter;

public class SigmaLines {

    SigmaLines(RadialPlot plot) throws Exception {
        this.plot = plot;
    }

    void plot() throws Exception {
        double[] xy0 = plot.getOrigin(),
                 xy1 = plot.rxry2xy(0, 2),
                 xy2 = plot.rxry2xy(0, -2),
                 xyc, xyU, xyL;
        double zcage;
        try {
            zcage = plot.getData().getPooledAge();
        } catch (Exception e){
            zcage = plot.getData().getArithmeticMean()[0];
        }
        double zc = plot.t2z(zcage);
        int    r0 = plot.wmap(plot.get_r0()),
               x0 = plot.wmap(xy0[0]),
               y0 = plot.hmap(xy0[1]),
               y1 = plot.hmap(xy1[1]),
               y2 = plot.hmap(xy2[1]),
               dy = y0-y1;
        xyc = plot.rz2xy(plot.get_r0(),zc);
        int xc = plot.wmap(xyc[0]),
            yc = plot.hmap(xyc[1]);
        double a = -Math.atan((double)(yc-y0)/(double)(xc-x0)),
               b = Math.asin(dy*Math.sin(a+Math.PI/2)/r0),
               c = Math.asin(dy*Math.sin(a-Math.PI/2)/r0);
        int xu = x0 + (int) ((int) r0 * Math.cos(a + b)),
            yu = y0 - (int) ((int) r0 * Math.sin(a + b)),
            xl = x0 + (int) ((int) r0 * Math.cos(a + c)),
            yl = y0 - (int) ((int) r0 * Math.sin(a + c));
        plot.getGraphics().drawLine(x0,y1,xu,yu);
        plot.getGraphics().drawLine(x0,y2,xl,yl);
    }

    protected RadialPlot plot;

}