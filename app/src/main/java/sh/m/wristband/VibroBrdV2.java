package sh.m.wristband;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * VibroBrdV2 communicates with firmware V2, where the driver type (MOSFET/haptic)
 * is managed on-board (hardcoded in Arduino)
 */
public class VibroBrdV2 extends VibroBrdDRV {
    public VibroBrdV2(BluetoothSocket socket) {
        super(socket);
    }
    //private buffer

    @Override
    public void setMotorPercentage(int motor, double value) throws IOException {
        writeBytes(String.format("SET;%d;%d;\r",motor,value*100).getBytes("US-ASCII"));
    }

    @Override
    public void setEnabled(boolean en) throws IOException {
        writeBytes(String.format("EN;%d;\r",en).getBytes("US-ASCII"));
    }

    /**
     * setLRA should perhaps throw exception in case the LRA option
     * is not available (MOSFET version). The info could be obtained in the constructor with
     * requestInfo(). However, if this is the case Arduino will return an error anyway
     * @param value
     * @throws IOException
     */
    @Override
    public void setLRA(boolean value) throws IOException, UnsupportedOperationException {
        writeBytes(String.format("LRA;%d;\r",value).getBytes("US-ASCII"));
    }

    public void requestInfo() throws IOException {
        writeBytes(String.format("INFO;%d;\r",true).getBytes("US-ASCII"));
    }
    public void setSequence(byte [] seq) throws IOException {
        String cmd="SQ;";
        for (byte b : seq) {
           cmd+=b+",";
        }
        cmd+=";\r";
        writeBytes(cmd.getBytes("US-ASCII"));
    }
    public void setWave(double amp, int tOn, int dir) throws IOException{
        writeBytes(String.format("W2P;%d;%d;\r",amp*100,tOn).getBytes("US-ASCII"));
        writeBytes(String.format("WEN;%d;\r",dir).getBytes("US-ASCII"));
    }


    /*
    def setEnable(self,b):
        self.sendCmd('%s;%d'%(DriverBrd.CMD_ENABLE,b),True)

    def setLra(self,b):
        self.sendCmd('%s;%d'%(DriverBrd.CMD_SET_LRA,b),True)

    def getInfo(self):
        return self.sendCmd(DriverBrd.CMD_INFO,True)

    def setValue(self, motor, value, wait=True):
        return self.sendCmd('%s;%d;%d'%(DriverBrd.CMD_SETVAL,motor,value),wait)

    def setSequence(self,seq):
        cmd='%s;%i;%s'%(DriverBrd.CMD_SQ,len(seq),','.join(str(x) for x in seq))
        #print cmd
        return self.sendCmd(cmd)

    def setWave(self, amp,tOn,wdir):
        self.sendCmd('%s;%d;%d'%(DriverBrd.WAVE_2P,tOn,amp),False)
        return self.sendCmd('%s;%d'%(DriverBrd.WAVE_EN,wdir),True)
     */
}
