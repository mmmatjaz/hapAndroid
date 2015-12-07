package sh.m.wristband;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * VibroBrd implements communication using the original 2 Byte firmware
 */
public class VibroBrd extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    protected final OutputStream mmOutStream;
    private List<WbBtListener> listeners=new ArrayList<>();

    protected static final int CMD_ENABLE=42;
    protected static final char CMD_DISABLE=43;
    private static final char CMD_OK='K';
    private static final char CMD_ERROR='E';
    protected static final char CMD_SET_LRA=43;
    protected static final char CMD_IS_DRV=40;

    //private byte [] outBuffer=new byte[2];
    public VibroBrd(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        Log.d("wb", "constructed");
    }


    public void run() {
        byte[] buffer = new byte[128];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                //        .sendToTarget();
                String str = new String(buffer, "US-ASCII").substring(0, bytes);
                Log.d("wb","rec: len="+ bytes + " txt=" +
                        str);
                for (WbBtListener l : listeners) {
                    l.wbResponded(str);
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    /* internal */
    protected void writeBytes(byte[] bytes) throws IOException{
        mmOutStream.write(bytes);
        Log.d("wb","sent: "+bytes[0]+" "+bytes[1] +" = "+new String(bytes, "US-ASCII"));
    }

    /* enable drivers */
    public void setEnabled(boolean e) throws IOException{
        Log.d("wb","enable "+e);
        writeBytes(new byte[]{CMD_ENABLE, e ? (byte) 1 : 0});
    }

    /* set individual motor power rate [0 1] */
    public void setMotorPercentage(int motor, double value)  throws IOException {
        //outBuffer[0]= (byte) motor;
        //outBuffer[1]= (byte) (value*255);
        writeBytes(new byte[]{(byte) motor, (byte) (value*255f)});
    }
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
        for (WbBtListener l : listeners) {
            l.connectionLost();
        }
    }

    public void setListener(WbBtListener l) {
        listeners.add(l);
    }

    public void rmListener(WbBtListener l) {
        listeners.remove(l);
    }
    public interface WbBtListener {
        /**
         * These methods are called from the background thread. If the listener
         * writes to the GUI, remember to use a scheduler!
         * @param msg
         */
        void wbResponded(String msg);
        void connectionLost();
    }
}