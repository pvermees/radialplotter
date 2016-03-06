package RadialPlotter;

import java.awt.Color;
import java.text.DecimalFormat;

public final class ColourScale {

    public ColourScale(RadialPlot plot) throws Exception {
        this.plot = plot;
        this.bottommargin = plot.BOTTOMMARGIN/3;
        this.height = plot.BOTTOMMARGIN/6;
        this.width = 1 - plot.getLeftMargin() - plot.RIGHTMARGIN;
        this.colours = new int[3][this.numcolours];
        this.initColours();
        this.setMinMaxC();
    }

    /*
     * sets minimum and maximum values of the Dpar or Cl parameter
     */
    public void setMinMaxC() throws Exception {
        double[] minmaxc = plot.getMinMaxC();
        this.minC = minmaxc[0];
        this.maxC = minmaxc[1];        
    }

    public void plot() throws Exception {
        this.initColours();
        this.plotBox();
        this.plotLabels();
    }
    
    private void plotLabels() throws Exception {
        float x1 = (float)(plot.wmap(plot.getLeftMargin() - 3*plot.getTicklength())),
              x2 = x1 + (float)(width*plot.getWidth() + plot.wmap(3*plot.getTicklength())),
              y = (float) (plot.hmap(bottommargin/6));
        DecimalFormat f = new DecimalFormat("#0.00");
        plot.getGraphics().drawString(f.format(this.minC),x1,y);
        plot.getGraphics().drawString(f.format(this.maxC),x2,y);
        plot.getGraphics().drawString(plot.data.preferences.zlabel(),(x1+x2)/2,y);
    }

    public void initColours() throws Exception {
        Color min = plot.data.preferences.minbarcolour(),
              max = plot.data.preferences.maxbarcolour();
        for (int i = 0; i<numcolours; i++){
            colours[0][i] = (int)(min.getRed() + (max.getRed() - min.getRed())*i/numcolours);
            colours[1][i] = (int)(min.getGreen() + (max.getGreen() - min.getGreen())*i/numcolours);
            colours[2][i] = (int)(min.getBlue() + (max.getBlue() - min.getBlue())*i/numcolours);
        }
    }
    
    public int[][] getColours() throws Exception {
        return colours;
    }
    
    private void plotBox() throws Exception {
        Color c;
        int x = plot.wmap(plot.getLeftMargin()),
            x0 = x,
            y = plot.hmap(bottommargin),
            w = (int)(width*plot.getWidth())/this.numcolours,
            h = (int)(height*plot.getHeight());
        for (int i=0; i<this.numcolours; i++){
            c = new Color(colours[0][i],colours[1][i],colours[2][i]);
            plot.getGraphics().setColor(c);
            plot.getGraphics().fillRect(x, y, w, h);            
            x += w;
            if (i+1<numcolours){
                w = (int)((width*plot.getWidth()-(x-x0))/(this.numcolours-i-1));
            }
        }
        plot.getGraphics().setColor(Color.BLACK);
        x = plot.wmap(plot.getLeftMargin());
        w = (int)(width*plot.getWidth());
        plot.getGraphics().drawRect(x,y,w,h);
    }

    public double getHeight(){
        return height;
    }
    
    // returns a [3xn] array where n is the number of colours
    public int[][] getColourScale() {
        return colours;
    }
    
    // colours must be a [3xn] array where n is the number of colours
    public void setColourScale(int[][] colours){
        this.colours = colours;
    }
    
    public int getNumColours(){
        return numcolours;
    }
    
    // also automatically initializes the colour scale
    public void setNumColours(int numcolours) throws Exception {
        this.numcolours = numcolours;
        this.initColours();
    }
    
    // returns an array of 3 integers between 0 and 255
    public Color getColour(double val) throws Exception {
        int colournum = (int) Math.floor(0.01+numcolours*0.98*Math.abs(val-minC)/(maxC-minC));
        Color c = new Color(colours[0][colournum],colours[1][colournum],colours[2][colournum]);
        return c;
    }  
    
    protected RadialPlot plot;
    protected double bottommargin, height, width;
    private double minC, maxC;
    private int numcolours = 100;
    private int[][] colours;
}
