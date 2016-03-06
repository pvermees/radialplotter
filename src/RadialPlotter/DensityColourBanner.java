package RadialPlotter;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

public final class DensityColourBanner extends javax.swing.JPanel {

    /** Creates new form DensityColourBanner
     * @param plot
     * @throws java.lang.Exception */
    public DensityColourBanner(DensityPlot plot) throws Exception {
        initComponents();
        this.prefs = plot.getData().preferences;
        ButtonGroup buttons = new ButtonGroup();
        buttons.add(this.KDEareaButton);
        buttons.add(this.KDElineButton);
        buttons.add(this.PDPareaButton);
        buttons.add(this.PDPlineButton);
        buttons.add(this.HistAreaButton);
        buttons.add(this.HistLineButton);
        buttons.add(this.PointsAreaButton);
        buttons.add(this.PointsLineButton);
        this.KDEareaButton.setSelected(true);
        this.plot = plot;
        setKDEbackground(this.prefs.KDEfillcolour());
        setKDEforeground(this.prefs.KDEstrokecolour());
        setPDPbackground(this.prefs.PDPfillcolour());
        setPDPforeground(this.prefs.PDPstrokecolour());
        setHistBackground(this.prefs.histfillcolour());
        setHistForeground(this.prefs.histstrokecolour());
        setPointsBackground(this.prefs.pointsfillcolour());
        setPointsForeground(this.prefs.pointstrokecolour());
        this.setSize();
    }

    public void setDefaults() throws Exception{
        setKDEbackground(Preferences.KDEAREACOLOUR);
        setKDEforeground(Preferences.KDELINECOLOUR);
        setPDPbackground(Preferences.PDPAREACOLOUR);
        setPDPforeground(Preferences.PDPLINECOLOUR);
        setHistBackground(Preferences.HISTAREACOLOUR);
        setHistForeground(Preferences.HISTLINECOLOUR);
        setPointsBackground(Preferences.POINTSAREACOLOUR);
        setPointsForeground(Preferences.POINTSLINECOLOUR);
    }

    private void setSize(){
        setPreferredSize(new Dimension(500, 86));
    }    
    
      public boolean doKDEarea() {
        return this.KDEareaButton.isSelected();
      }

      public boolean doKDEline() {
        return this.KDElineButton.isSelected();
      }

      public boolean doPDParea() {
        return this.PDPareaButton.isSelected();
      }

      public boolean doPDPline() {
        return this.PDPlineButton.isSelected();
      }

      public boolean doHistArea() {
        return this.HistAreaButton.isSelected();
      }

      public boolean doHistLine() {
        return this.HistLineButton.isSelected();
      }

      public boolean doPointsArea() {
        return this.PointsAreaButton.isSelected();
      }

      public boolean doPointsLine() {
        return this.PointsLineButton.isSelected();
      }

      public void setKDEbackground(Color colour) throws Exception {
        this.prefs.KDEfillcolour(colour);
        this.KDEpreview.setBackground(colour);
      }

      public void setKDEforeground(Color colour) throws Exception {
        this.prefs.KDEstrokecolour(colour);
        this.KDEpreview.setBorder(BorderFactory.createLineBorder(colour, 2));
      }

      public void setPDPbackground(Color colour) throws Exception {
        this.prefs.PDPfillcolour(colour);
        this.PDPpreview.setBackground(colour);
      }

      public void setPDPforeground(Color colour) throws Exception {
        this.prefs.PDPstrokecolour(colour);
        this.PDPpreview.setBorder(BorderFactory.createLineBorder(colour, 2));
      }

      public void setHistBackground(Color colour) throws Exception {
        this.prefs.histfillcolour(colour);
        this.HistPreview.setBackground(colour);
      }

      public void setHistForeground(Color colour) throws Exception {
        this.prefs.histstrokecolour(colour);
        this.HistPreview.setBorder(BorderFactory.createLineBorder(colour, 2));
      }

      public void setPointsBackground(Color colour) throws Exception {
        this.prefs.pointsfillcolour(colour);
        this.PointsPreview.setBackground(colour);
      }

      public void setPointsForeground(Color colour) throws Exception {
        this.prefs.pointstrokecolour(colour);
        this.PointsPreview.setBorder(BorderFactory.createLineBorder(colour, 2));
      }

    public JPanel getPreview(){
        return KDEpreview;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        KDEpreview = new javax.swing.JPanel();
        PointsLineButton = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        HistLineButton = new javax.swing.JRadioButton();
        HistAreaButton = new javax.swing.JRadioButton();
        PDPareaButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        PDPpreview = new javax.swing.JPanel();
        PointsPreview = new javax.swing.JPanel();
        HistPreview = new javax.swing.JPanel();
        PDPlineButton = new javax.swing.JRadioButton();
        PointsAreaButton = new javax.swing.JRadioButton();
        KDElineButton = new javax.swing.JRadioButton();
        KDEareaButton = new javax.swing.JRadioButton();

        KDEpreview.setBorder(new javax.swing.border.MatteBorder(null));

        javax.swing.GroupLayout KDEpreviewLayout = new javax.swing.GroupLayout(KDEpreview);
        KDEpreview.setLayout(KDEpreviewLayout);
        KDEpreviewLayout.setHorizontalGroup(
            KDEpreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 105, Short.MAX_VALUE)
        );
        KDEpreviewLayout.setVerticalGroup(
            KDEpreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        PointsLineButton.setText("stroke");

        jLabel4.setText("Points");

        jLabel3.setText("Hist");

        HistLineButton.setText("stroke");

        HistAreaButton.setText("fill");

        PDPareaButton.setText("fill");

        jLabel2.setText("PDP");

        jLabel1.setText("KDE");

        PDPpreview.setBorder(new javax.swing.border.MatteBorder(null));

        javax.swing.GroupLayout PDPpreviewLayout = new javax.swing.GroupLayout(PDPpreview);
        PDPpreview.setLayout(PDPpreviewLayout);
        PDPpreviewLayout.setHorizontalGroup(
            PDPpreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 105, Short.MAX_VALUE)
        );
        PDPpreviewLayout.setVerticalGroup(
            PDPpreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        PointsPreview.setBorder(new javax.swing.border.MatteBorder(null));

        javax.swing.GroupLayout PointsPreviewLayout = new javax.swing.GroupLayout(PointsPreview);
        PointsPreview.setLayout(PointsPreviewLayout);
        PointsPreviewLayout.setHorizontalGroup(
            PointsPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 111, Short.MAX_VALUE)
        );
        PointsPreviewLayout.setVerticalGroup(
            PointsPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        HistPreview.setBorder(new javax.swing.border.MatteBorder(null));

        javax.swing.GroupLayout HistPreviewLayout = new javax.swing.GroupLayout(HistPreview);
        HistPreview.setLayout(HistPreviewLayout);
        HistPreviewLayout.setHorizontalGroup(
            HistPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 117, Short.MAX_VALUE)
        );
        HistPreviewLayout.setVerticalGroup(
            HistPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        PDPlineButton.setText("stroke");

        PointsAreaButton.setText("fill");

        KDElineButton.setText("stroke");

        KDEareaButton.setText("fill");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jLabel1)
                        .addGap(90, 90, 90)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(49, 49, 49))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(KDEareaButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(KDElineButton))
                            .addComponent(KDEpreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(PDPareaButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PDPlineButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(PDPpreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, Short.MAX_VALUE)
                                .addComponent(HistAreaButton, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HistLineButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(HistPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(PointsAreaButton, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PointsLineButton, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE))
                            .addComponent(PointsPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(jLabel3)
                        .addGap(94, 94, 94)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                        .addGap(34, 34, 34)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(PointsPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(HistPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(KDEpreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(PDPpreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(KDEareaButton)
                    .addComponent(KDElineButton)
                    .addComponent(PDPareaButton)
                    .addComponent(PDPlineButton)
                    .addComponent(HistAreaButton)
                    .addComponent(HistLineButton)
                    .addComponent(PointsAreaButton)
                    .addComponent(PointsLineButton)))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {HistPreview, KDEpreview, PDPpreview, PointsPreview});

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton HistAreaButton;
    private javax.swing.JRadioButton HistLineButton;
    private javax.swing.JPanel HistPreview;
    private javax.swing.JRadioButton KDEareaButton;
    private javax.swing.JRadioButton KDElineButton;
    private javax.swing.JPanel KDEpreview;
    private javax.swing.JRadioButton PDPareaButton;
    private javax.swing.JRadioButton PDPlineButton;
    private javax.swing.JPanel PDPpreview;
    private javax.swing.JRadioButton PointsAreaButton;
    private javax.swing.JRadioButton PointsLineButton;
    private javax.swing.JPanel PointsPreview;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables

    protected DensityPlot plot;
    protected Preferences prefs;

}