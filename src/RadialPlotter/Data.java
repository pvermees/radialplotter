package RadialPlotter;

import java.util.*;
import java.io.*;

public abstract class Data implements Iterator, Iterable {
    
    public Data(String fn, Preferences preferences) {
        X = new ArrayList<Double>();
        Y = new ArrayList<Double>();
        W = new ArrayList<Double>();
        z = new ArrayList<Double>();
        sigma = new ArrayList<Double>();
        C = new ArrayList<Double>();
        radialX = new ArrayList<Double>(); 
        radialY = new ArrayList<Double>();
        setFileName(fn);
        this.preferences = preferences;
    }

    public Preferences preferences(){
        return preferences;
    }
    
    /* check to see if the input file contains -as its second entry-
     * "F" (Fission tracks = 0), "L" (Luminescence = 1) or "O" (Other = 2).
     * If not, return -1
     */
    static public boolean isFissionTrackFile(String fn) throws Exception {
        String aLine;
        char datatype = 'F';
        try {
            //System.out.println("Loading: " + fn);
            FileInputStream fin = new FileInputStream(fn);
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));
            // extract sample name
            aLine = br.readLine();
            StringTokenizer stokenizer = new StringTokenizer(aLine, ",");
            int numtokens = stokenizer.countTokens();
            stokenizer.nextToken();
            if (numtokens == 1) {
                return false;
            } else if (numtokens == 2){
                datatype = stokenizer.nextToken().charAt(0);
            }
            return datatype=='F';
        } catch (IOException e){
            System.err.println("Problem with Data.getDataType()");
            return false;
        }
    }
    
    public double log(double num) {
        return Math.log(num + offset);
    }
        
    public double exp(double num) {
        return Math.exp(num) - offset;
    }
    
    public double log10(double num) {
        return Math.log10(num + offset);
    }
    
    public double exp10(double num) {
        return Math.pow(10, num) - offset;
    }
    
    public double logerr(double num, double err) {
        return err / (num + offset);
    }
    
    public double experr(double num, double err) {
        return err * (exp(num) + offset);
    }

    public void readData() throws Exception {
        String aLine;
        if (filename.equals("")){return;}
        try {
            //System.out.println("Loading: " + filename);
            FileInputStream fin = new FileInputStream(getFileName());
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));
            // extract sample name
            aLine = br.readLine();
            StringTokenizer stokenizer = new StringTokenizer(aLine, ",");
            int numtokens = stokenizer.countTokens();
            samplename = stokenizer.nextToken();
            if (numtokens == 2){
                stokenizer.nextToken();
            }  
            readHeaderAndBody(br);
            br.close();
        } catch (FileNotFoundException e){
            System.err.println("Couldn't find file: " + filename);
            this.setFileName("");
        } catch (Exception e) {
            System.err.println("Problem in Data:readData()");
        }
    }

    int getRowNumber(int samplenumber) throws Exception {
        int sn = 0;
        for (Iterator i = iterator(); i.hasNext(); i.next()) {
            if (sn >= samplenumber){
                break;
            } else {
                sn++;
            }
        }
        return ii;
    }
    
    abstract void readHeaderAndBody(BufferedReader br) throws Exception;
    
    public void readBody(BufferedReader br) throws Exception {
        String aLine;
        int numtokens;
        try {
            // extract the X, (optional) Y and (optional) W data
            for (int i=0; (aLine = br.readLine()) != null ; i++) {
                StringTokenizer st = new StringTokenizer(aLine, ",");
                numtokens = st.countTokens();
                this.setX(Double.parseDouble(st.nextToken()),i);
                if (numtokens > 1) {
                    this.setY(Double.parseDouble(st.nextToken()),i);
                } else {
                    this.setY(NAN, i);
                }
                if (numtokens == 3) {
                    this.setW(Double.parseDouble(st.nextToken()),i);
                } else {
                    this.setW(NAN, i);
                }
            }
        } catch (IOException e){
            System.err.println("Error in Data.readBody(), error reading: " + filename);
        } catch (NumberFormatException e) {
            System.err.println("Error in Data.readBody(), error reading: " + filename);
        }
    }
    
    abstract void data2rxry(boolean fixedAxes) throws Exception ;
    
    abstract void writeOutput(String filepath) throws Exception ;
    
    protected void writeBody(BufferedWriter out) throws Exception {
        String nl = System.getProperties().getProperty("line.separator");
        try {
            double[] XYW;
            double c;
            for (Iterator i= this.iterator(); i.hasNext(); ) {
                XYW = (double[]) i.next();
                c = XYW[2];
                this.writeXY(out, XYW[0], XYW[1]);
                if (c >=0) {
                    out.write("," + c);
                }
                out.write(nl);
            }
            out.close();
        } catch (IOException e) {
            System.err.println("Problem in Data.writeBody()");
        }
    }
    
    protected void writeXY(BufferedWriter out, double X, double Y) throws Exception{
        out.write(X + "," + Y);
    }
    
    /* returns 3-element array of central age, its standard error and age dispersion*/
    abstract double[] getCentralAge() throws Exception ;
    
    abstract double getPooledAge() throws Exception;    

    abstract double[] getArithmeticMean() throws Exception;

    public double[] getMinMaxAgeErr(boolean doLog) throws Exception {
        double[][] dea = this.getDataErrArray(doLog);
        double[] minmaxterr = {MINT,MAXT, 0, 0}; // initialise to some ridiculous values
        for (int i=0; i<dea[0].length; i++){
            if (dea[0][i]<minmaxterr[0]){
                minmaxterr[0] = dea[0][i];
                minmaxterr[2] = dea[1][i];
            }
            if (dea[0][i]>minmaxterr[1]){
                minmaxterr[1] = dea[0][i];
                minmaxterr[3] = dea[1][i];
            }
        }
        return minmaxterr;
    };

    double[] getMinMaxAge() throws Exception{
        double[] minmaxterr = getMinMaxAgeErr(false),
                 out = {minmaxterr[0],minmaxterr[1]};
        return out;
    }

    double getMinAge() throws Exception {
        double[] mM = getMinMaxAge();
        return mM[0];
    }

    double getMaxAge() throws Exception {
        double[] mM = getMinMaxAge();
        return mM[1];
    }
    
    double[] getMinMaxC() throws Exception {
        double c;
        double[] minmaxc = {1e10,1e-10};// initialize to some ridiculous values
        double[] XYW;
        for (Iterator i= iterator(); i.hasNext(); ) {
            XYW = (double[]) i.next();
            c = XYW[2];
            if (c<minmaxc[0] && c!= Data.NAN){
                minmaxc[0] = c;
            } 
            if (c>minmaxc[1] && c!= Data.NAN){
                minmaxc[1] = c;
            }
        }
        return minmaxc;    
    };   
    
    public double getC(int index) throws Exception {
        return C.get(index);
    }
    
//    Reloads the data from filename if you have changed it
    public void reload() throws Exception {
        this.clear();
        readData();
    }

    public void clear() throws Exception {
        X.clear();
        Y.clear();
        W.clear();
        z.clear();
        sigma.clear();
        C.clear();
        radialX.clear();
        radialY.clear();
    }
    
    public int getSize(){
        return X.size();
    }

    public double getY(int index) {
        return Y.get(index);
    }

    public void setY(double value, int index) {
        // recursively extend the ArrayList if necessary
        if (index<Y.size()){
            Y.set(index, value);
        } else {
            X.add(NAN);
            Y.add(NAN);
            W.add(NAN);
            setY(value,index);
        }
    }
    
    public double getX(int index) {
        return X.get(index);
    }
    
    public void setX(double value, int index) {
        // recursively extend the ArrayList if necessary
        if (index<X.size()){
            X.set(index, value);
        } else {
            X.add(NAN);
            Y.add(NAN);
            W.add(NAN);
            setX(value,index);
        }
    }

    public double getW(int index) {
        return W.get(index);
    }
    
    public void setW(double value, int index) {
        // recursively extend the ArrayList if necessary
        if (index<W.size()){
            W.set(index, value);
        } else {
            X.add(NAN);
            Y.add(NAN);
            W.add(NAN);
            setW(value,index);
        }
    }    
    
    public String getSampleName(){
        return samplename;
    }
    
    public void setSampleName(String samplename){
        this.samplename = samplename;
    }  
    
    public void insertEntry(double x, double y, int index) throws Exception {
        this.insertEntry(x, y, NAN, index);
    }

    public void insertEntry(double x, double y, double c, int index) throws Exception {
        X.add(index, x);
        Y.add(index, y);
        W.add(index, c);
    }
    
    public void removeEntry(int index) throws Exception {
        Y.remove(index);
        X.remove(index);
        W.remove(index);
    }
    
    private void setFileName(String fn){
        filename = fn;
    }
    
    public String getFileName(){
        return filename;
    }
    
    /* checks to see if there are any instances where X = 0
     * (to be used to automatically assign arcsin transformation)
     */
    public boolean hasZeros() throws Exception {
        double xyj;
        double[] XY;
        for (Iterator i= this.iterator(); i.hasNext(); ){
            XY = (double[]) i.next();
            xyj = XY[0];
            if (xyj == 0){
                return true;
            }
        }
        return false;
    }

    abstract double[][] getDataErrArray(boolean doLog) throws Exception;

    abstract double z2t(double z0) throws Exception ;
    
    abstract double t2z(double t) throws Exception ;

    abstract double[] zs2ts(double z, double s) throws Exception ;
    
    int count(double min, double max) throws Exception {
        double[][] ae = this.getDataErrArray(false);
        int n = 0;
        for (int i=0; i<ae[0].length; i++){
            if (ae[0][i] >= min & ae[0][i] <= max) n++;
        }
        return n;
    }
    
    public double[] zs2rxry(double z, double sigma) throws Exception {
        double[] xy = new double[2];
        xy[0] = 1/sigma;
        xy[1] = (z-z0)/sigma;
        return xy;
    }

    public double[] rxry2zs(double rx, double ry) throws Exception {
        double[] zs = new double[2];
        zs[1] = 1/rx;
        zs[0] = z0 + zs[1]*ry;
        return zs;
    }    
    
    public ArrayList<Double> getRadialX(){
        return this.radialX;
    }

    public ArrayList<Double> getRadialY(){
        return this.radialY;
    }

    /* getW the radial parameter z (not to be confused with the colour parameter W!)*/
    public ArrayList<Double> get_z(){
        return this.z;
    }

    public ArrayList<Double> get_sigma(){
        return this.sigma;
    }

    public double get_z0(){
        return this.z0;
    }    

    public void set_z0(double z0){
        this.z0 = z0;
    }
    
    /* checks to see if there are at least 2 instances where the third
     * data column !=0, in order to see if a colour scale should be plotted
     */
    public boolean hasColour() throws Exception {
        double zj;
        int numGTzero = 0; // number of instances where z>=0
        double[] XYW;
        for (Iterator i= this.iterator(); i.hasNext(); ){
            XYW = (double[]) i.next();
            zj = XYW[2];
            if (zj >= 0){
                numGTzero++;
            }
            if (numGTzero>=2){return true;}
        }
        return false;
    }

    public int length(){
       int j = 0;
       for (Iterator i = this.iterator(); i.hasNext(); j++){ i.next(); }
       return j;
    }

    public int getNumPeaks() throws Exception {
        return numpeaks;
    }

    public void setNumPeaks(int numpeaks) throws Exception {
        this.numpeaks = numpeaks;
    }
    
    public void copy(Data data){
        this.samplename = data.samplename;
        double[] XYW;
        int j = 0;
        for (Iterator i= data.iterator(); i.hasNext(); ) {
            XYW = (double[]) i.next();
            this.setX(XYW[0], j);
            this.setY(XYW[1], j);
            this.setW(XYW[2], j);
            j++;
        }
    }

    @Override
    public Iterator iterator() {
        ii = 0;
        // if the X, Y arrays are empty, return immediately
        if (this.X.isEmpty()){return this;}
        // if the first row is empty, find the first non-empty row
        if (this.getX(ii)==NAN){
            do {
            ii++;
            } while (hasNext() && (this.getX(ii)==NAN));
            return this;
        } else {
            return this;
        }
    }

    @Override
    public boolean hasNext() { return ii < this.getSize(); }

    @Override
    public double[] next() {
        double xx = this.getX(ii);
        double yy = this.getY(ii);
        double ww = this.getW(ii);
        double[] XYW = {xx,yy,ww};
        // put iterator in right position for next request
        do {
            ii++;
        } while (hasNext() && (this.getX(ii)==NAN));
        return XYW;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() in Data.java not implemented");
    }
   
   protected String filename, samplename = "";
   protected ArrayList<Double> Y, X, W, C, z, sigma, radialX, radialY;
   protected int ii; // iterator index
   protected double z0 = 0.0, offset = 0.0;
   static final double NAN = -999.9, MINT = Double.MAX_VALUE, MAXT = -Double.MAX_VALUE;
   static final boolean debugmode = false;  
   protected Preferences preferences;
   protected int numpeaks = 0;
   final static double GA = 1e9, MA = 1e6, KA = 1e3, A = 1;

}