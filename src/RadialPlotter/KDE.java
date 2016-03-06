package RadialPlotter;

import java.util.Arrays;

public class KDE {

    public void KDE(){
        this.reset();
    }
    
    public void reset(){
        this.bandwidth = Data.NAN;
    }

    public double getBandwidth(){
        return this.bandwidth;
    }

    public void setBandwidth(double bandwidth){
        this.bandwidth = bandwidth;
    }
    
    public void setBandwidth(Data data, boolean auto) {
        try {
            double[] x = data.getDataErrArray(data.preferences.logarithmic())[0];
            this.setBandwidth(x,auto);
        } catch (Exception e) {
            this.bandwidth = Data.NAN;
        }
    }

    public void setBandwidth(double[] x, boolean auto) throws Exception{
        int N = unique(x); // number of unique data points
        Arrays.sort(x);
        double[] xmesh = getMesh(x[0],x[x.length-1]);
        double R = xmesh[xmesh.length-1]-xmesh[0];
        double[] initial_data = arrayDiv(histc(x,xmesh),x.length);
        double[] a = dct1d(initial_data);
        double tstar = auto ? fsolve(a,N) : (bandwidth*bandwidth)/(R*R);
        bandwidth = Math.sqrt(tstar)*R; 
    }
    
    // get the density rescaled so that the maximum value is 1
    public double[] getKDE(Data data, double[] timescale, boolean autoBandwidth) throws Exception {
        double[][] ae = data.getDataErrArray(data.preferences.logarithmic());
        boolean adaptive = data.preferences.adaptive();
        boolean epanechnikov = data.preferences.epanechnikov();
        return (this.getKDE(ae[0], timescale, autoBandwidth, adaptive, epanechnikov));
    }  
    
    public double[] getKDE(double[] ages, double[] timescale, 
           boolean autoBandwidth, boolean adaptive, boolean epanechnikov) throws Exception {
        double[] pdf;
        this.setBandwidth(ages,autoBandwidth);
        if (adaptive){
            pdf = this.adapt(ages,timescale,epanechnikov);
        } else {
            pdf = this.pdf(ages,timescale,epanechnikov);
        }
        return pdf;        
    }
    
    private double[] getMesh(double mint, double maxt) throws Exception {
        double[] xmesh = new double[nt];
        double dt, minx, maxx;
        minx = mint-(maxt-mint)/5;
        maxx = maxt+(maxt-mint)/5;
        dt = (maxx-minx)/(nt-1);
        for (int i=0; i<nt; i++){
            xmesh[i] = minx + i*dt;
        }
        return xmesh;
    }
            
    private double[] getMesh(Data data) throws Exception {
        boolean doLog = data.preferences.logarithmic();
        double[] minmaxt = data.getMinMaxAge(), 
                 xmesh = new double[nt];
        double dt, minx, maxx;
        if (doLog){
            minx = minmaxt[0]/2;
            maxx = minmaxt[1]*2;
            dt = (data.log(maxx)-data.log(minx))/(nt-1);
        } else {
            minx = minmaxt[0]-(minmaxt[1]-minmaxt[0])/5;
            maxx = minmaxt[1]+(minmaxt[1]-minmaxt[0])/5;
            dt = (maxx-minx)/(nt-1);
        }
        for (int i=0; i<nt; i++){
            xmesh[i] = doLog ? data.log(minx) + i*dt : minx + i*dt;
        }
        return xmesh;
    }

    private static int unique(double[] data) throws Exception {
        int n = data.length;
        int N = (n==0) ? 0 : 1;
        double[] sortedData = data.clone();
        Arrays.sort(sortedData);
        for (int i=1; i<n; i++){
            if (sortedData[i]!=sortedData[i-1]) {
                N++;
            }
        }
        return N;
    }

    private static double[] arrayDiv(double[] data, double N) throws Exception {
        int n = data.length;
        double[] out = new double[n];
        for (int i=0; i<n; i++){
            out[i] = data[i]/N;
        }
        return out;
    }

    private static Complex[] arrayMultiply(Complex[] array1, Complex[] array2) throws Exception {
        Complex[] out = new Complex[array1.length];
        for (int i=0; i<array1.length; i++){
            out[i] = array1[i].times(array2[i]);
        }
        return out;
    }

    private static double[] arrayReal(Complex[] array) throws Exception {
        int n = array.length;
        double[] out = new double[n];
        for (int i=0; i<n; i++){
            out[i] = array[i].re();
        }
        return out;
    }

    private static double[] histc(double[] data, double[] xmesh) throws Exception {
        int ii, n = xmesh.length;
        double min = xmesh[0], max = xmesh[n-1];
        double[] out = new double[n];
        for (int i=0; i<data.length; i++){
            if (data[i]>=min & data[i]<=max){
                ii = (int)Math.floor((n-1)*(data[i]-min)/(max-min));
                out[ii]++;
            }
        }
        return out;
    }

    private static double[] geta2(double[] a) throws Exception {
        int n = a.length;
        double[] a2 = new double[n-1];
        for (int i=1; i<n; i++){
            a2[i-1] = a[i]*a[i]/4;
        }
        return a2;
    }

    private static double[] getI(int n) throws Exception {
        double[] I = new double[n-1];
        for (int i=1; i<n; i++){
            I[i-1] = i*i;
        }
        return I;
    }

    private static double fsolve(double[] a, int N) {
        double t_star;
        try {
            double[] I = getI(a.length);
            double[] a2 = geta2(a);
            t_star = binarySearch(0,0.1,N,I,a2);
        } catch (Exception e) {
            t_star = 0.28*Math.pow(N,-2d/5d);
            System.err.println("KDE: binary search for t_star failed.");
        }
        return t_star;
    }

    private static double binarySearch(double m, double M, int N, double[] I, double[] a2) throws Exception {
        double t, misfit, min=m, max=M;
        // for loop and not recursive to prevent infinite loops
        for (int i=0; i<100; i++){
            t = min + 0.5*(max-min);
            misfit = fixed_point(t,(double)N,I,a2);
            if (Math.abs(misfit/t)<1e-3){
                return t;
            } else if (misfit>0d) {
                max = t;
            } else {
                min = t;
            }
        }
        return 0.28*Math.pow(N,-2d/5d);
    }

    private static double fixed_point(double t, double N, double[] I, double[] a2) throws Exception {
        // this implements the function t-zeta*gamma^[l](t)
        int l = 7;
        double K0, cnst, time;
        double f = getf(I,l,a2,t);
        for (int s=l-1; s>1; s--){
            K0 = getK0(s);
            cnst = (1+Math.pow(0.5,s+0.5))/3;
            time = Math.pow(2d*cnst*K0/N/f,2d/(3d+2d*s));
            f = getf(I,s,a2,time);
        }
        return (t-Math.pow(2d*N*Math.sqrt(Math.PI)*f,-2d/5d));
    }

    private static double getK0(int s) throws Exception {
        double K0 = 1d;
        for (int i=1; i<2*s; i=i+2){
            K0 *= i;
        }
        return K0/Math.sqrt(2*Math.PI);
    }

    private static double getf(double[] I, int l, double[] a2, double t) throws Exception {
        int n = I.length;
        double sum = 0d;
        for (int i=0; i<n; i++){
            sum += Math.pow(I[i],l)*a2[i]*Math.exp(-I[i]*Math.PI*Math.PI*t);
        }
        return 2*Math.pow(Math.PI,2*l)*sum;
    }

    private static double[] dct1d(double[] data) throws Exception {
        // computes the discrete cosine transform of the column vector data
        int n = data.length;
        double gamma;
        Complex[] data2 = new Complex[n];
        // Compute weights to multiply DFT coefficients
        Complex[] weight = new Complex[n];
        weight[0] = new Complex(1,0);
        for (int i=1; i<n; i++){
            gamma = -i*Math.PI/(2*n);
            weight[i] = new Complex(2*Math.cos(gamma),2*Math.sin(gamma));
        }
        // Re-order the elements of the columns of x
        for (int i=0; i<n/2; i++){
            data2[i] = new Complex(data[2*i],0);
            data2[n/2+i] = new Complex(data[n-1-2*i],0);
        }
        // Multiply FFT by weights:
        Complex[] fft = FFT.fft(data2);
        Complex[] weightedData = arrayMultiply(weight,fft);
        return arrayReal(weightedData);
    }

    public double[] pdf(double[] data, double[] timescale, boolean epanechnikov) throws Exception {
        double[] out = new double[timescale.length];
        for (int i=0; i<data.length; i++){
            for (int j=0; j<timescale.length; j++){
                out[j] += this.kernel(data[i], this.bandwidth, timescale[j], epanechnikov);
            }
        }
        return out;
    }    
    
    public double getG(double[] fX, boolean epanechnikov) throws Exception{
        int j=0, G=0;
        for (int i=0; i<fX.length; i++){
            if (fX[i]>0){
                G += ToolBox.log(fX[i]);
                j++;
            }
        }
        return Math.exp(G/j);
    }
    
    // implements the adaptive KDE algorithm of Abramson (1982) as described by Jann (2007)
     double[] adapt(double[] data, double[] timescale, boolean epanechnikov) throws Exception {
        double[] out = new double[timescale.length];
        double[] fX = this.pdf(data, data, epanechnikov);
        double G = getG(fX, epanechnikov);
        double lambda;
        for (int i=0; i<data.length; i++){
            if (fX[i]>0){
                lambda = Math.sqrt(G/fX[i]);
                for (int j=0; j<timescale.length; j++){
                    out[j] += this.kernel(data[i], bandwidth*lambda, timescale[j], epanechnikov);
                }
            }
        }
        return out;
    }
    
    public double kernel(double dat, double bw, double x, boolean epanechnikov) throws Exception {
        double out;
        if (epanechnikov){
            double u = Math.abs((dat-x)/bw);
            out =  (u>1) ? 0d : 0.75*(1-u*u)/bw;
        } else {
            out = Stat.gaussianPDF(dat, bw, x);
        }
        return out;
    }

    private double bandwidth;
    private final int nt = 512;

}