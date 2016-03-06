package RadialPlotter;

import java.util.ArrayList;

public class Newton {

/* calculates age average and overdispersion
 */
static double[] solveMuXi(ArrayList<double[]> tst) throws Exception {
    double[] MuXi = new double[2];
    double Mu = 0d, Xi = 0d, dMu;
    for (int i=0; i<n; i++){
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
    for (int i=0; i<n; i++){
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
    double f = 0d, df = 0d, a, a2, b, b2, b3, t, st;
    for (int i=0; i<tst.size(); i++){
        t = tst.get(i)[0];
        st = tst.get(i)[1];
        a = t-mu;
        a2 = a*a;
        b = st*st+xi;
        b2 = b*b;
        b3 = b2*b;
        f += (a2/b2 - 1/b);
        df += (1/b2 - 2*a2/b3);
    }
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

private static final int n = 100;

}