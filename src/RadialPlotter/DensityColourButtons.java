
package RadialPlotter;

import javax.swing.SwingUtilities;

public class DensityColourButtons extends javax.swing.JPanel {

    public DensityColourButtons(DensityColourBanner banner) {
        initComponents();
        this.banner = banner;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DefaultsButton = new javax.swing.JButton();
        OKbutton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(400, 29));

        DefaultsButton.setText("Defaults");
        DefaultsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DefaultsButtonActionPerformed(evt);
            }
        });

        OKbutton.setText("OK");
        OKbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKbuttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(125, Short.MAX_VALUE)
                .addComponent(DefaultsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(OKbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(DefaultsButton)
                .addComponent(OKbutton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void DefaultsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DefaultsButtonActionPerformed
        try {
            banner.setDefaults();
        } catch (Exception ex) {
            if (Data.DEBUGMODE){ex.printStackTrace(System.out);}
        }
    }//GEN-LAST:event_DefaultsButtonActionPerformed

    private void OKbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKbuttonActionPerformed
        SwingUtilities.getWindowAncestor(this).dispose();
    }//GEN-LAST:event_OKbuttonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton DefaultsButton;
    private javax.swing.JButton OKbutton;
    // End of variables declaration//GEN-END:variables
    protected DensityColourBanner banner;

}