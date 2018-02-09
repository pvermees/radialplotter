package RadialPlotter;

public class MinAgeMod {

    public MinAgeMod(Data data){
        this.data = data;
        gspbest = new double[3]; // gsp = gamma, sigma, pi
    }

    public void gridSearch() throws Exception {
        double[] tmM = data.getMinMaxAgeErr("logarithmic");
        try {
            int numages = data.length(),
                ng = 100, ns = 25, np = 10; // number of iterations for each parameter;
            double[][] ae = data.getDataErrArray("logarithmic");
            double ming = tmM[0],
                   maxg = tmM[1],
                   dg = (maxg-ming)/ng,
                   ds = 1d/ns,
                   dp = 1d/np;
            double[] gsp = {ming,1d,1d};
            double oldLL = LL(ae,numages,gsp), newLL;
            // loop through gamma at higher resolution
            for (int i=0; i<ng; i++){
                gsp[0] = ming + i*dg;
                for (int j=0; j<ns; j++){
                    gsp[1] = (j+1d)*ds;
                    for (int k=0; k<np; k++){
                        gsp[2] = (k+1d)/(np+1d);
                        newLL = LL(ae,numages,gsp);
                        if (newLL>oldLL){
                            gspbest = gsp.clone();
                            oldLL = newLL;
                        }
                    }
                }
            }
            this.cov(ae, dg/2d, ds/2d, dp/2d);
        } catch (Exception e){
            gspbest[0] = tmM[0];
            gammaErr = tmM[2];
        }
        gspbest[1] = data.experr(gspbest[0],gspbest[1]);
        gspbest[0] = data.exp(gspbest[0]);
        gammaErr = gammaErr*gspbest[0];
    }

    static double LL(double[][] ae, int numages, double[] gsp){
        double LL = 0d;
        for (int u=0; u<numages; u++){
            LL += Math.log(fu(ae[0][u], ae[1][u], gsp));
        }
        return LL;
    }

    static double fu(double zu, double su, double[] gsp){
        double gamma = gsp[0],
               sigma = gsp[1],
               pi = gsp[2],
               vu = su*su,
               v = sigma*sigma,
               mu0u = (gamma/v + zu/vu)/(1/v + 1/vu),
               s0u = 1/Math.sqrt(1/v + 1/vu),
               A = pi/Math.sqrt(2*Math.PI*vu),
               B = -(zu-gamma)*(zu-gamma)/(2*vu),
               C = (1-pi)/Math.sqrt(2*Math.PI*(v+vu)),
               D = (1-Stat.normalCDF(0d,1d,(gamma-mu0u)/s0u))/(1-Stat.normalCDF(0d,1d,0d)),
               E = -(zu-gamma)*(zu-gamma)/(2*(v+vu));
        return A*Math.exp(B) + C*D*Math.exp(E);
    }

    public double[] getMinAgeErr() throws Exception {
        double[] out = new double[2];
        out[0] = gspbest[0];
        out[1] = gammaErr;
        return out;
    }

    private void cov(double[][] ae, double dg, double ds, double dp) throws Exception {
        int n = data.length();
        double g = gspbest[0], s = gspbest[1], p = gspbest[2];
        double[][] J = new double[3][3]; // Jacobian
        J[0][0] = -(LL(ae,n,gsp(g+dg,s,p))-2*LL(ae,n,gspbest)+LL(ae,n,gsp(g-dg,s,p)))/(dg*dg);
        J[1][1] = -(LL(ae,n,gsp(g,s+ds,p))-2*LL(ae,n,gspbest)+LL(ae,n,gsp(g,s-ds,p)))/(ds*ds);
        J[2][2] = -(LL(ae,n,gsp(g,s,p+dp))-2*LL(ae,n,gspbest)+LL(ae,n,gsp(g,s,p-dp)))/(dp*dp);
        J[0][1] = -(LL(ae,n,gsp(g+dg,s+ds,p))-LL(ae,n,gsp(g+dg,s-ds,p))-LL(ae,n,gsp(g-dg,s+ds,p))+LL(ae,n,gsp(g-dg,s-ds,p)))/(4*dg*ds);
        J[0][2] = -(LL(ae,n,gsp(g+dg,s,p+dp))-LL(ae,n,gsp(g+dg,s,p-dp))-LL(ae,n,gsp(g-dg,s,p+dp))+LL(ae,n,gsp(g-dg,s,p-dp)))/(4*dg*dp);
        J[1][2] = -(LL(ae,n,gsp(g,s+ds,p+dp))-LL(ae,n,gsp(g,s+ds,p-dp))-LL(ae,n,gsp(g,s-ds,p+dp))+LL(ae,n,gsp(g,s-ds,p-dp)))/(4*ds*dp);
        J[1][0] = J[0][1];
        J[2][0] = J[0][2];
        J[2][1] = J[1][2];
        double det = J[0][0]*J[1][1]*J[2][2] + J[1][0]*J[2][1]*J[0][2] + J[2][0]*J[0][1]*J[1][2] -
                     J[0][0]*J[2][1]*J[1][2] - J[2][0]*J[1][1]*J[0][2] - J[1][0]*J[0][1]*J[2][2];
        double gammaVar = (J[2][2]*J[1][1] - J[1][2]*J[2][1])/det;
        if (!(gammaVar>0)) {
            throw new Exception();
        } else {
            gammaErr = Math.sqrt(gammaVar);
        }
    }

    private double[] gsp(double g, double s, double p){
        double[] out = {g, s, p};
        return out;
    }

    protected Data data;
    protected double[] gspbest;
    protected double gammaErr;
}