package RadialPlotter;

import java.util.*;
import java.io.*;

public class OtherData extends Data implements Iterator, Iterable {
    
    public OtherData(Preferences preferences){
        this("", preferences);
    }
    
    public OtherData(String fn, Preferences preferences){
        super(fn, preferences);
        try {
            this.readData();
        } catch (Exception e){
            if (debugmode){e.printStackTrace(System.out);}
        }
    }
    
    public static OtherData cast(Data olddata) throws Exception {
        OtherData newdata = new OtherData(olddata.preferences);
        newdata.copy(olddata);
        return newdata;
   }

    @Override
    void readHeaderAndBody(BufferedReader br) throws Exception {
        super.readBody(br);
    }

    @Override
    double z2t(double z) throws Exception {
        double t = 0;
        if (preferences.linear()){
            t = z;
        } else if (preferences.logarithmic()){
            t = this.exp(z);
        }
        return t;
    }
    
    @Override
    double t2z(double t) throws Exception {
        double zout = 0d;
        if (preferences.linear()){
            zout = t;
        } else if (preferences.logarithmic()){
            zout = this.log(t);
        }
        return zout;
    }    
    
    @Override
    double[] zs2ts(double z, double s) throws Exception {
        double[] ts = new double[2];
        if (preferences.linear()){
            ts[0] = z;
            ts[1] = s;
        } else if (preferences.logarithmic()){
            ts[0] = this.exp(z);
            ts[1] = this.experr(z, s);
        }
        return ts;
    }

    @Override
    /* populates the radialX, radialY, zout, sigma, and C ArrayLists with data*/
    public void data2rxry(boolean fixedAxes) throws Exception {
        radialX.clear();
        radialY.clear();
        C.clear();
        z.clear();
        sigma.clear();
        double zj = 0.0, sigmaj = 0.0, age, age_err;
        double[] XYW;
        // calculate the zout and sigma values for each grain
        for (Iterator i= this.iterator(); i.hasNext(); ) {
            XYW = (double[]) i.next();
            age = XYW[0];
            age_err = XYW[1];
            C.add(XYW[2]);
            if (preferences.linear()){
                zj = age;
                sigmaj = age_err;
            } else if (preferences.logarithmic()){
                zj = this.log(age);
                sigmaj = this.logerr(age, age_err);
            }
            z.add(zj);
            sigma.add(sigmaj);
        }
        // calculate the central value (z)
        if (!fixedAxes) {
            double[] tsd = new double[3];
            if (preferences.linear()){
                tsd = this.getArithmeticMean();
            } else if (preferences.logarithmic()){
                tsd = this.getGeometricMean();
            }
            this.set_z0(this.t2z(tsd[0]));
        }
        // calculate and add the corresponding rx and ry values
        for (int i=0;i<z.size();i++){
            double[] rxry = zs2rxry(z.get(i),sigma.get(i));
            radialX.add(rxry[0]);
            radialY.add(rxry[1]);
        }
    }

    @Override
    void writeOutput(String filepath) throws Exception {
        String nl = System.getProperties().getProperty("line.separator");
        BufferedWriter out = new BufferedWriter(new FileWriter(filepath));
        out.write(samplename + "," + "O" + nl);
        super.writeBody(out);
    }

    @Override
    /* in this case, return the weighted geometric mean*/
    double[] getCentralAge() throws Exception {
        return this.getGeometricMean();
    }

    @Override
    double getPooledAge() throws Exception {
        double[] ts = getCentralAge();
        return ts[0];
    }    
    
    /* return the weighted arithmetic mean*/
    double[] getGeometricMean() throws Exception {
        return getMean(true);
    }

    /* return the weighted arithmetic mean*/
    @Override
    double[] getArithmeticMean() throws Exception {
        return getMean(false);
    }

    double[] getMean(boolean geometric) throws Exception {
        ArrayList<double[]> tst = new ArrayList<double[]>();
        double[] AgeErr, MuXi, out = new double[3];
        double Var, dispersion;
        for (Iterator i= this.iterator(); i.hasNext(); ) {
            AgeErr = (double[]) i.next();
            if (geometric) {
                AgeErr[1] = this.logerr(AgeErr[0],AgeErr[1]);
                AgeErr[0] = this.log(AgeErr[0]);
            }
            tst.add(AgeErr);
        }
        MuXi = Newton.solveMuXi(tst);
        Var = Newton.gettv(tst,MuXi[1]);
        dispersion = Math.sqrt(MuXi[1]);
        if (geometric){
            out[0] = this.exp(MuXi[0]);
            out[1] = this.experr(MuXi[0], Math.sqrt(Var));
        } else {
            out[0] = MuXi[0];
            out[1] = Math.sqrt(Var);
        }
        out[2] = dispersion<0.00001 ? 0 : dispersion;
        return out;
    }

    public double getAgeErr(int index) throws Exception {
        return super.getY(index);
    }
    
    public void setAgeErr(double value, int index) throws Exception {
        super.setY(value, index);
    }
    
    public double getAge(int index) throws Exception {
        return super.getX(index);
    }
    
    public void setAge(double value, int index) throws Exception {
        super.setX(value, index);
    }
    
    public void insertEntry(int ns, int ni, int index) throws Exception {
        super.insertEntry((double)ns, (double)ni, NAN, index);
    }

    public void insertEntry(int ns, int ni, double c, int index) throws Exception {
        super.insertEntry((double) ns, (double) ni, c, index);
    }

    @Override
    protected double[][] getDataErrArray(boolean doLog) throws Exception {
        double[] ae;
        double[][] ages = new double[2][this.length()],
                   logages = new double[2][this.length()], out;
        int j = 0;
        for (Iterator i= iterator(); i.hasNext(); j++) {
            ae = (double[]) i.next();
            ages[0][j] = ae[0];
            ages[1][j] = ae[1];
            logages[1][j] = this.logerr(ages[0][j],ages[1][j]);
            logages[0][j] = this.log(ages[0][j]);
        }
        out = doLog ? logages : ages;
        return out;
    }

}
