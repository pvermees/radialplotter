package RadialPlotter;

import java.util.ArrayList;

public class Newton {

/* calculates age average and overdispersion
 */
static double[] solveMuXi(ArrayList<double[]> tst) throws Exception {
    double[] MuXi = new double[2];
    double Mu = 0d, Xi = 0d, dMu;
    for (int i=0; i<N; i++){
        MuXi[0] = Mu;
        MuXi[1] = Xi;
        Mu = solveMu(tst,Xi);
        Xi = solveXi(tst,Mu);
        dMu = Mu - MuXi[0];
        if (doBreak(dMu,MuXi[0])){
            break;
        }
    }
    return MuXi;
}

static double solveMu(ArrayList<double[]> tst, double Xi) throws Exception {
    double ti, si, num = 0d, denom = 0d;
    for (int i=0; i<tst.size(); i++){
        ti = tst.get(i)[0];
        si = tst.get(i)[1];
        num += ti/(si*si + Xi);
        denom += 1/(si*si + Xi);
    }
    return num/denom;
}

static double solveXi(ArrayList<double[]> tst, double mu) throws Exception {
    double xi = 0d, dxi;
    double[] fdf = getfdf(tst,mu,xi);
    // exit with zero if xi=0 has positive slope
    if (fdf[1] > 0){return xi;}
    for (int i=0; i<N; i++){
        fdf = getfdf(tst,mu,xi);
        dxi = fdf[0]/fdf[1];
        xi -= dxi;
        // xi must be positive
        if (xi<0){
            return 0;
        }
        if (doBreak(dxi,xi)){
            return xi;
        }
    }
    return xi;
}

static double[] getfdf(ArrayList<double[]> tst, double mu, double xi){
    double f, df, zu, su, wu, LH = 0d, RH = 0d, dwu, dLH = 0d, dRH = 0d;
    for (int u=0; u<tst.size(); u++){
        zu = tst.get(u)[0];
        su = tst.get(u)[1];
        wu = 1/(su*su+xi);
        LH += Math.pow(wu*(zu-mu),2);
        RH += wu;
        dwu = -wu*wu;
        dLH += 2*(zu-mu)*dwu;
        dRH += dwu;
    }
    f = Math.pow(1-LH/RH,2);
    df = 2*(1-LH/RH)*(dLH*RH-LH*dRH)/(RH*RH);
    double[] fdf = {f,df};
    return fdf;
}

public static double gettv(ArrayList<double[]> tst, double Xi) throws Exception {
    double denom = 0d, st;
    for (int i=0; i<tst.size(); i++){
        st = tst.get(i)[1];
        denom += 1/(st*st + Xi);
    }
    return 1/denom;
}

static private boolean doBreak(double ds, double s) throws Exception {
    boolean out = (s!=0 && Math.abs(ds/s)<1e-5);
    return out;
}

private static final int N = 100;

}