package RadialPlotter;

import Jama.Matrix;
import java.util.Iterator;
import java.util.Random;

public final class BinomFit {

    public BinomFit(Data data) {
        this.data = data;
        pi = new double[MNC][MNC];
        theta = new double[MNC][MNC];
        L = new double[MNC];
        r = new Random();
        minimod = new MinAgeMod(data);
        try {
            this.findPeaks(data.getNumPeaks());
        } catch (Exception e) {
            // do nothing
        }
    }

    public void findPeaks(int k) throws Exception{
        if (k<0){
            minimod.gridSearch(); // minimum age model
        } else {
            BinoMetropolis(k); // binomfit
        }
    }

    public void BinoMetropolis(int k) throws Exception{
        double Lnew, Lold, die, randnum;
        double[] ithetabest, ithetaold, ithetanew;
        initialise(k); // initialises theta (and pi)
        ithetabest = theta[k-1].clone();
        ithetaold = theta[k-1].clone();
        L[k-1] = iterate(k, theta[k-1]); // initialises L
        if (k<3){return;}
        Lold = L[k-1];
        for (int i=0; i<k*10; i++){
            ithetanew = modify(k, ithetaold);
            Lnew = iterate(k, ithetanew);
            if (Lnew > Lold){
                if (Lnew > L[k-1]){
                    L[k-1] = Lnew;
                    ithetabest = ithetanew.clone();
                }
                Lold = Lnew;
                ithetaold = ithetanew.clone();
            } else {
                die = Math.exp(Lnew-Lold);
                randnum = r.nextDouble();
                if (randnum < die){
                    Lold = Lnew;
                    ithetaold = ithetanew.clone();
                }
            }
        }
        // leave the program with the best fit
        iterate(k,ithetabest);    
    }

    public double[] getPeakAgeErr(int i) throws Exception {
        double[] ae = new double[2];
        int numpeaks = data.getNumPeaks();
        if (data instanceof FTdata){
            FTdata ftdata = (FTdata)data;
            double NsNi = theta[numpeaks-1][i]/(1-theta[numpeaks-1][i]);
            double betaErr2 = cov.get(numpeaks-1+i,numpeaks-1+i);
            ae[0] = FT.getFTage(ftdata.getZeta(),ftdata.getRhoD(),NsNi, 1);
            ae[1] = ae[0]*Math.sqrt( betaErr2 +
                    Math.pow(ftdata.getRhoD_Err()/ftdata.getRhoD(),2) +
                    Math.pow(ftdata.getZeta_Err()/ftdata.getZeta(),2));
        } else {
            ae[0] = data.exp(theta[numpeaks-1][i]);
            ae[1] = data.experr(theta[numpeaks-1][i],
                                Math.sqrt(cov.get(numpeaks-1+i,numpeaks-1+i)));
        }
        return ae;
    }

    public void auto() throws Exception {
        double bic, BIC = Double.MAX_VALUE;
        for (int i=0; i<MNC; i++){
            findPeaks(i+1);
            bic = -2*L[i] + (2*i)*ToolBox.log(data.getSize());
            if (bic<BIC){
                BIC = bic;
                this.setNumPeaks(i+1);
            }
        }
        findPeaks(this.getNumPeaks());
    }

    public double[] modify(int k, double[] in) throws Exception {
        double[] out = in.clone();
        int i = 1 + r.nextInt(k-2);
        double m = in[i-1], M = in[i+1];
        out[i] = m + r.nextDouble()*(M-m);
        return out;
    }

    public void initialise(int k) throws Exception {
        double[] minmaxt = data.getMinMaxAge();
        double minz, maxz, beta;
        if (data instanceof FTdata){
            maxz = FT.getz((FTdata)(data), minmaxt[1]);
            minz = (minmaxt[0]== 0) ? maxz-2 : FT.getz((FTdata)(data), minmaxt[0]);
        } else {
            maxz = data.log(minmaxt[1]);
            minz = (minmaxt[0]== 0) ? maxz-2 : data.log(minmaxt[0]);
        }
        for (int i=0; i<k; i++){
            pi[k-1][i] = 1.0/k;
            beta = (k>1) ? minz + i*(maxz-minz)/(k-1): minz;
            if (data instanceof FTdata){
                theta[k-1][i] = Math.exp(beta)/(1+Math.exp(beta));
            } else {
                theta[k-1][i] = beta; // in Galbraith's book, this theta is called beta
            }
        }
    }

    // K = # components, T = # array of initial peak locations
    public double iterate(int k, double[] T) throws Exception {
        double LL = 0;
        int u=0, n = data.length();
        double[] m = new double[n], x = new double[n], 
                 y = new double[n], f2 = new double[k];
        double num, denom, zu, su, maxexp = 0d, MAXEXP = 0d;
        double[][] f = new double[k][n], p = new double[k][n],
                   a = new double[k][n], b = new double[k][n];
        theta[k-1] = T.clone();
        try {
        // x, y, and m are constant for all iterations so are set here
        double[] XY;
        for (Iterator i=data.iterator(); i.hasNext(); u++){
            XY = (double[]) i.next();
            if (data instanceof FTdata){
                y[u] = XY[0];
                m[u] = XY[0] + XY[1];
            } else {
                zu = data.log(XY[0]);
                su = data.logerr(XY[0],XY[1]);
                x[u] = 1/su;
                y[u] = zu/su;
            }
        }
        // 20 iterations should be enough to ensure convergence
        for (int j=0; j<20; j++) {
            if (data instanceof OtherData){
                MAXEXP = getMaxExp(x, y, k, n); // overall maximum
            }
            // calculate p[i][u]
            for (u=0; u<n; u++){
                denom = 0;
                if (data instanceof OtherData){
                    maxexp = getMaxExp(x[u], y[u], k); // maximum for grain u
                }
                if (data instanceof FTdata){
                    for (int i=0; i<k; i++){
                        // f[i][u] = Stat.binomialPDF(theta[k-1][i],m[u],y[u]);
                        f[i][u] = treshold(Math.pow(theta[k-1][i],y[u])*Math.pow(1-theta[k-1][i],m[u]-y[u]));
                        a[i][u] = y[u] - theta[k-1][i]*m[u];
                        b[i][u] = Math.pow(y[u]-theta[k-1][i]*m[u],2) -
                                  theta[k-1][i]*(1-theta[k-1][i])*m[u];
                        denom += pi[k-1][i]*f[i][u];
                    }
                    for (int i=0; i<k; i++){
                        p[i][u] = pi[k-1][i]*f[i][u]/denom;
                    }
                } else {
                    for (int i=0; i<k; i++){
                        f[i][u] = treshold(Math.exp(-0.5*Math.pow(y[u]-theta[k-1][i]*x[u],2)-MAXEXP));
                        a[i][u] = x[u]*(y[u]-theta[k-1][i]*x[u]);
                        b[i][u] = -x[u]*x[u]*(1-Math.pow(y[u]-theta[k-1][i]*x[u],2));
                        f2[i] = Math.exp(-0.5*Math.pow(y[u]-theta[k-1][i]*x[u],2)-maxexp);
                        denom += pi[k-1][i]*f2[i];
                    }
                    for (int i=0; i<k; i++){
                        p[i][u] = pi[k-1][i]*f2[i]/denom;
                    }
                }
            }
            // update pi[i] and theta[i]
            for (int i=0; i<k; i++){
                pi[k-1][i] = 0; // reset pi before update
                num = 0;
                denom = 0;
                for (u=0; u<n; u++){
                    pi[k-1][i] += p[i][u]/n;
                    if (data instanceof FTdata){
                        num += p[i][u]*y[u];
                        denom += p[i][u]*m[u];
                    } else {
                        num += p[i][u]*x[u]*y[u];
                        denom += p[i][u]*x[u]*x[u];
                    }
                }
                theta[k-1][i] = num/denom;
            }
            // calculate Likelihood
            LL = 0;
            for (u=0; u<n; u++){
                num = 0;
                for (int i=0; i<k; i++){
                    num += pi[k-1][i]*f[i][u];
                }
                LL += ToolBox.log(num);
            }
        }
        cov = covariance(k, n, p, pi, a, b);
        return LL; // add arbitrary constant to getW a positive number
        } catch (Exception e){
            if (Data.DEBUGMODE){ e.printStackTrace(System.out); }
            return -Double.MAX_VALUE;
        }
    }

    private double treshold(double in){
        double out = (in==0) ? 1e-100 : in ;
        return out;
    }

    private double getMaxExp(double xu, double yu, int k) throws Exception{
        double exp, maxexp = -0.5*Math.pow(yu-theta[k-1][0]*xu,2);
        for (int i=1; i<k; i++){
            exp = -0.5*Math.pow(yu-theta[k-1][i]*xu,2);
            if (exp > maxexp){
                maxexp = exp;
            }
        }
        return maxexp;
    }

    private double getMaxExp(double[] x, double[] y, int k, int n) throws Exception{
        double exp, maxexp = -0.5*Math.pow(y[0]-theta[k-1][0]*x[0],2);
        for (int u=1; u<n; u++){
            exp = this.getMaxExp(x[u], y[u], k);
            if (exp > maxexp){
                maxexp = exp;
            }
        }
        return maxexp;
    }

    // must run findPeaks first
    public Matrix covariance(int k, int n, double[][] p, double[][] pi, double[][] a, double[][] b) throws Exception {
        Matrix M = new Matrix(2*k-1,2*k-1);
        Matrix A = new Matrix(k-1,k-1);
        Matrix B = new Matrix(k-1,k);
        Matrix C = new Matrix(k,k);
        double foo;
        // getW matrix A
        for (int u=0; u<n; u++){
            for (int i=0; i<k-1; i++){
                for (int j=0; j<k-1; j++){
                    foo = (p[i][u]/pi[k-1][i]-p[k-1][u]/pi[k-1][k-1])*
                          (p[j][u]/pi[k-1][j]-p[k-1][u]/pi[k-1][k-1]);
                    A.set(i,j,A.get(i,j)+foo);
                }
            }
            for (int i=0; i<k-1; i++){
                for (int j=0; j<k; j++){
                    foo = p[j][u]*a[j][u]*
                          (p[i][u]/pi[k-1][i]-p[k-1][u]/pi[k-1][k-1]-
                           delta(i,j)/pi[k-1][j]+delta(j,k-1)/pi[k-1][k-1]);
                    B.set(i,j,B.get(i,j)+foo);
                }
            }
            for (int i=0; i<k; i++){            
                for (int j=0; j<k; j++){
                    foo = p[i][u]*p[j][u]*a[i][u]*a[j][u]-
                          delta(i,j)*b[i][u]*p[i][u];
                    C.set(i,j,C.get(i,j)+foo);
                }
            }
        }
        M.setMatrix(0,k-2,0,k-2,A);
        M.setMatrix(0,k-2,k-1,2*k-2,B);
        M.setMatrix(k-1,2*k-2,0,k-2,B.transpose());
        M.setMatrix(k-1,2*k-2,k-1,2*k-2,C);
        return M.inverse();
    }

    private int delta(int i, int j){
        if (i==j) {
            return 1;
        } else {
            return 0;
        }
    }

    public double getPerr(int i, int k){
        double perr;
        if (i==k-1){
            double ss = 0;
            for (int j=0; j<k-1; j++){
                ss += cov.get(j,j);
            }
            perr = 100*Math.sqrt(ss);
        } else {
            perr = 100*Math.sqrt(cov.get(i,i));
        }
        return perr;
    }

    public double[][] getPi() throws Exception {
        return pi;
    }
    
    public double[][] getTheta() throws Exception {
        return theta;
    }

    public int getNumPeaks() throws Exception {
        return data.getNumPeaks();
    }

    public void setNumPeaks(int k) throws Exception {
        data.setNumPeaks(k);
    }

    public MinAgeMod getMinAgeMod() {
        return minimod;
    }
    
    protected Data data;
    protected double[][] pi, theta;
    protected double[] L;
    Matrix cov;
    final private int MNC = 5; // maximum number of components
    private Random r;
    private MinAgeMod minimod;
}