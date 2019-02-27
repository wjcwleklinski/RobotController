package com.contoller.wojtek.robotcontroller;


import Jama.Matrix;

public class KalmanFilter {

    private double dt = 0.2;
    private double noiseV = 1., noiseW = 5.;
    private Matrix ARoll, BRoll, CRoll, VRoll, WRoll, x0Roll, P0Roll, xpriRoll, PpriRoll, xpostRoll, PpostRoll;
    private Matrix APitch, BPitch, CPitch, VPitch, WPitch, x0Pitch, P0Pitch, xpriPitch, PpriPitch, xpostPitch, PpostPitch;
    private Matrix AYaw, BYaw, CYaw, VYaw, WYaw, x0Yaw, P0Yaw, xpriYaw, PpriYaw, xpostYaw, PpostYaw;
    private double[] accMeas, gyroMeas, mgnMeas;
    private boolean firstRun = true;


    public KalmanFilter() {

        initMatrixes();
    }

    private void initMatrixes() {
        ARoll = new Matrix(new double[][] {{1., -dt}, {0., 1.}});
        APitch = ARoll;
        AYaw = ARoll;
        BRoll = new Matrix(new double[][] {{dt}, {0.}});
        BPitch = BRoll;
        BYaw = BRoll;
        CRoll = new Matrix(new double[] {1., 0.}, 1);
        CPitch = CRoll;
        CYaw = CRoll;

        VRoll = new Matrix(new double[][] {{noiseV * noiseV * dt, 0}, {0, noiseV * noiseV * dt}});
        VPitch = VRoll;
        VYaw = VRoll;
        WRoll = new Matrix(new double[] {noiseW * noiseW}, 1);
        WPitch = WRoll;
        WYaw = WRoll;

        x0Roll = new Matrix(new double[][] {{0}, {0}});
        x0Pitch = x0Roll;
        x0Yaw = x0Roll;
        P0Roll = new Matrix(new double[][] {{1, 0}, {0, 1}});
        P0Pitch = P0Roll;
        P0Yaw = P0Roll;
        xpriRoll = x0Roll;
        xpriPitch = x0Pitch;
        xpriYaw = x0Yaw;
        PpriRoll = P0Roll;
        PpriPitch = P0Pitch;
        PpriYaw = P0Yaw;
        xpostRoll = x0Roll;
        xpostPitch = x0Pitch;
        xpostYaw = x0Yaw;
        PpostRoll = P0Roll;
        PpostPitch = P0Pitch;
        PpostYaw = P0Yaw;
    }

    public void setAccMeasurements(float[] inAcc) {
        // casting floats to doubles
        double[] temp = new double[3];
        for(int i = 0; i < temp.length; i++)
            temp[i] = inAcc[i];
        this.accMeas = temp;
    }

    public void setGyroMeasurements(float[] inGyro) {
        // casting floats to doubles
        double[] temp = new double[3];
        for(int i = 0; i < temp.length; i++)
            temp[i] = inGyro[i];
        this.gyroMeas = temp;
    }

    public void setMgnMeasurements(float[] inMgn) {
        // casting floats to doubles
        double[] temp = new double[3];
        for(int i = 0; i < temp.length; i++)
            temp[i] = inMgn[i];
        this.mgnMeas = temp;
    }

    /**
     * Computes single angle roll, pitch or yaw depending on what is given in constructor
     * @param inAcc
     * @param inGyro
     * @param inMgn
     */
    public float[] computeAngles(float[] inAcc, float[] inGyro, float[] inMgn) {

        double accRoll = Math.atan2(inAcc[1], inAcc[0]);
        double accPitch = -1 * Math.atan2(inAcc[2], Math.sqrt(inAcc[1] * inAcc[1] + inAcc[0] * inAcc[0]));
        double accYaw = Math.atan2(Math.sin(accRoll) * inMgn[0] - Math.cos(accRoll) * inMgn[1],
                Math.cos(accPitch) * inMgn[2] + Math.sin(accRoll) * Math.sin(accPitch) * inMgn[1] + Math.cos(accRoll) *
                Math.sin(accPitch) * inMgn[0]);// * 180/Math.PI;

        //accRoll = accRoll * 180/Math.PI;
        //accPitch = accPitch * 180/Math.PI;
        Matrix YRoll = new Matrix(new double[] {accRoll}, 1);
        Matrix YPitch = new Matrix(new double[] {accPitch}, 1);
        Matrix YYaw = new Matrix(new double[] {accYaw}, 1);

        double uGyroRoll = inGyro[2];
        double uGyroPitch = inGyro[1];
        double uGyroYaw = inGyro[0];
        if(firstRun) {
            firstRun = false;
            xpostRoll = new Matrix(new double[][] {{accRoll}, {0}});
            xpostPitch = new Matrix(new double[][] {{accPitch}, {0}});
            xpostYaw = new Matrix(new double[][] {{accYaw}, {0}});
        }
        xpriRoll = ARoll.times(xpostRoll).plus(BRoll.times(uGyroRoll));
        xpriPitch = APitch.times(xpostPitch).plus(BPitch.times(uGyroPitch));
        xpriYaw = AYaw.times(xpostYaw).plus(BYaw.times(uGyroYaw));
        PpriRoll = ARoll.times(PpostRoll).times(ARoll.transpose()).plus(VRoll);
        PpriPitch = APitch.times(PpostPitch).times(APitch.transpose()).plus(VPitch);
        PpriYaw = AYaw.times(PpostYaw).times(AYaw.transpose()).plus(VYaw);

        Matrix epsRoll = YRoll.minus(CRoll.times(xpriRoll));
        Matrix epsPitch = YPitch.minus(CPitch.times(xpriPitch));
        Matrix epsYaw = YYaw.minus(CYaw.times(xpriYaw));
        Matrix SRoll = CRoll.times(PpriRoll).times(CRoll.transpose()).plus(WRoll);
        Matrix SPitch = CPitch.times(PpriPitch).times(CPitch.transpose()).plus(WPitch);
        Matrix SYaw = CYaw.times(PpriYaw).times(CYaw.transpose()).plus(WYaw);
        Matrix KRoll = PpriRoll.times(CRoll.transpose()).times(SRoll.inverse());
        Matrix KPitch = PpriPitch.times(CPitch.transpose()).times(SPitch.inverse());
        Matrix KYaw = PpriYaw.times(CYaw.transpose()).times(SYaw.inverse());

        xpostRoll = xpriRoll.plus(KRoll.times(epsRoll));
        xpostPitch = xpriPitch.plus(KPitch.times(epsPitch));
        xpostYaw = xpriYaw.plus(KYaw.times(epsYaw));
        PpostRoll = PpriRoll.minus(KRoll.times(SRoll).times(KRoll.transpose()));
        PpostPitch = PpriPitch.minus(KPitch.times(SPitch).times(KPitch.transpose()));
        PpostYaw = PpriYaw.minus(KYaw.times(SYaw).times(KYaw.transpose()));

        return (new float[] {(float)xpostYaw.get(0,0), (float)xpostPitch.get(0,0), (float)xpostRoll.get(0,0)});
    }





}
