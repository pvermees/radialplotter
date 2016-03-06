package RadialPlotter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

public final class PlotPanel extends javax.swing.JPanel{
    
    /** Creates new form PlotPanel
     * @param data
     * @throws java.lang.Exception */
    public PlotPanel(Data data) throws Exception {
        
        initComponents();
                
        this.refresh(data);
        
        setBorder(BorderFactory.createLineBorder(Color.black));

        addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {             
                try {
                    if (e.isControlDown()) {
                        this.rescale(e);
                    } else {
                        this.PopUp(e);
                    }
                } catch (Exception ex){
                    if (Data.debugmode){ex.printStackTrace(System.out);}
                }
            }

            private void rescale(MouseEvent e) throws Exception {
                if (plotRadial) {
                    radialplot.setRightMargin(e.getX());
                } else {
                    densityplot.setMaxY(e.getY());
                }
                repaint();
            }

            private void PopUp(MouseEvent e) throws Exception {
                if (plotRadial){
                    radialplot.PopUp(e);
                } else {
                    densityplot.PopUp(e);
                }
            }
            
        });

    }

    public Plot getPlot() throws Exception {
        if (plotRadial){
            return radialplot;
        } else {
            return densityplot;
        }
    }

    public void setDimension(int fact) {
        if (plotRadial){
            this.setSize(new Dimension(fact*RADIALWIDTH,fact*RADIALHEIGHT));
        } else {
            this.setSize(new Dimension(fact*DENSITYWIDTH,fact*DENSITYHEIGHT));
        }
    }
    
    public void refresh(Data data) throws Exception{
        plotRadial = data.preferences.radialplot();
        if (plotRadial){
            this.radialRefresh(data);
            this.setPreferredSize(new Dimension(RADIALWIDTH,RADIALHEIGHT));
        } else {
            this.densityRefresh(data);
            this.setPreferredSize(new Dimension(DENSITYWIDTH,DENSITYHEIGHT));
        }
        this.setSize(this.getPreferredSize());        
    }
    
    public void radialRefresh(Data data) throws Exception {
        if (radialplot == null) {
            radialplot = new RadialPlot(data);
        } else {
            radialplot.refresh(data);
        } 
    }
    
    public void densityRefresh(Data data) throws Exception {
        if (densityplot == null) {
            densityplot = new DensityPlot(data);
        } else {
            densityplot.refresh(data);
        }        
    }
    
    @Override
    public void paintComponent(Graphics g) {
        try {
            g2 = (Graphics2D) g;
            double scale = getScale();
            myfont = g2.getFont().deriveFont(AffineTransform.getScaleInstance(scale, scale));
            g2.setFont(myfont);
            super.paintComponent(g2);
            if (plotRadial){
                radialplot.setGraphics(g2,this.getWidth(),this.getHeight());
                radialplot.plotData();
                radialplot.plotRadialScale();
                radialplot.plotAxes();
                radialplot.printLegend();
                if (radialplot.data.preferences.abanico()){
                    radialplot.plotKDE();
                }
                radialplot.plotPeaks();
                radialplot.plotMarkers();
                if (radialplot.getData().hasColour()){
                    radialplot.plotColourScale();
                }
                if (radialplot.getData().preferences.sigmalines()){
                    radialplot.plotSigmaLines();
                }
           } else {
                densityplot.setGraphics(g2, this.getWidth(), this.getHeight());
                if (densityplot.firstRun()){
                    densityplot.setDefault();
                }
                densityplot.plotHist();
                densityplot.plotKDE();
                densityplot.plotPDP();
                densityplot.plotPoints();
                densityplot.plotBells();
                densityplot.plotAxes();
                densityplot.plotPeaks();
                densityplot.printLegend();
                densityplot.plotMarkers();
           }
        } catch (Exception e){
            if (plotRadial){
                radialplot.printStackTrace(e);
            } else {
                densityplot.printStackTrace(e);
            }
        }
    }

    private double getScale(){
        double width = (double) this.getWidth(),
               height = (double) this.getHeight(),
               defaultwidth = plotRadial ? (double)RADIALWIDTH : (double)DENSITYWIDTH,
               defaultheight = plotRadial ? (double)RADIALHEIGHT : (double)DENSITYHEIGHT,
               scalex = width/defaultwidth,
               scaley = height/defaultheight;
        if (scalex<scaley){
            return scalex;
        } else {
            return scaley;
        }
    }
    
    public RadialPlot getRadialPlot(){
        return this.radialplot;
    }

    public DensityPlot getDensityPlot(){
        return this.densityplot;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 496, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 496, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected RadialPlot radialplot;
    protected DensityPlot densityplot;
    private Graphics2D g2;
    private boolean plotRadial;
    private Font myfont;
    final int RADIALWIDTH = 450, RADIALHEIGHT = 450,
              DENSITYWIDTH = 650, DENSITYHEIGHT = 450;

}