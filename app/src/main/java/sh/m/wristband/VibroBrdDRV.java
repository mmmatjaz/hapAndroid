package sh.m.wristband;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * VibroBrdDRV implements communication using the original 2 Byte firmware
 * for boards with the haptic driver
 */
public class VibroBrdDRV extends VibroBrd{
    public VibroBrdDRV(BluetoothSocket socket) {
        super(socket);
    }


    @Override
    public void setMotorPercentage(int motor, double value) throws IOException {
        writeBytes(new byte[]{(byte) motor, (byte) (((value*127f))+127)});
    }

    public void setLRA(boolean value) throws IOException {
        writeBytes(new byte[]{(byte) CMD_SET_LRA, (byte) (value ? 1:0)});
    }
}
