package sh.m.wristband;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ClipData;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,VibroBrd.WbBtListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private List<BluetoothDevice> pairedDevices=new ArrayList<>();
    private VibroBrd wb=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not available.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this,
                    "Please enable your BT and re-run this program.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Set<BluetoothDevice> devs = mBluetoothAdapter.getBondedDevices();
        pairedDevices.addAll(devs);

        List<String> devList = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices) {
            devList.add(bt.getName());
            Log.d("bt", bt.getName() + " = " + bt.getAddress() + " = " + bt.getBondState());
        }
        //devList.add("Disconnect");
        if (pairedDevices.size() > 0)
            invalidateOptionsMenu();

        setFragmentVisibility(false);
    }

    private void setFragmentVisibility(boolean b) {
        findViewById(R.id.container).setVisibility(b? View.VISIBLE : View.INVISIBLE);
    }
    private VibroBrd getWb() throws NotYetConnectedException {
        if (wb == null) {
            Log.d("wb", "wb is null");
            throw new NotYetConnectedException();
        }
        else return wb;
    }

    public boolean trySetVal(int name, float value) {
        try {
            wb.setMotorPercentage(name, value);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setFragmentVisibility(false);
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    public boolean trySetEnable(boolean value) {
        try {
            //wb.write((byte) name, (byte) value);
            wb.setEnabled(value);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setFragmentVisibility(false);
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menu.clear();
        int i=0;
        for (BluetoothDevice d : pairedDevices) {
            menu.add(0,i,i++,d.getName());
        }
        return true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        try {
            fragmentManager.beginTransaction()
                .replace(R.id.container,
                    (android.support.v4.app.Fragment) NavigationDrawerFragment
                        .menuOptions[position].newInstance())
                .commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void onSectionAttached(int number) {

        mTitle=NavigationDrawerFragment.menuOptionNames[number];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        Log.d("menu", "item id: "+id + " name: "+item.getTitle());
        //new ConnectThread(BluetoothAdapter.getDefaultAdapter(),pairedDevices.get(id)).start();
        try {
            Log.d("bt", pairedDevices.get(id).getAddress());
        } catch (Exception e) {
            return super.onOptionsItemSelected(item);
        }
        // disconnect current device

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        //if (id>=pairedDevices.size())
        //    return super.onOptionsItemSelected(item);

        if (wb!=null)
            wb.cancel();

        // connect new device
        new AsyncTask<Void, Void, BluetoothSocket>() {
            private final String riderStr="00001101-0000-1000-8000-00805f9b34fb";
            private final ParcelUuid rider=ParcelUuid.fromString(riderStr);
            String copy=null;
            public ProgressDialog dialog;

            @Override
            protected BluetoothSocket doInBackground(Void... params) {
                BluetoothSocket mmSocket = null;
                BluetoothDevice mmDevice=pairedDevices.get(id);
                int sdk = Build.VERSION.SDK_INT;

                if (sdk >= 10) {
                    ParcelUuid theID;
                    if (sdk >= 15) {
                        ParcelUuid[] ids = mmDevice.getUuids();
                        for (ParcelUuid id : ids) {
                            Log.d("bt", "uuid" + id.toString());
                        }
                        theID=ids[0];
                    }
                    else { // old devices
                        theID=rider;
                    }

                    // Get a BluetoothSocket to connect with the given BluetoothDevice
                    try {
                        // MY_UUID is the app's UUID string, also used by the server code
                        //mmSocket = mmDevice.createRfcommSocketToServiceRecord(
                        //       ids[0].getUuid()/*UUID.fromString("")*/);
                        mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(theID.getUuid());
                        Log.d("bt", "createRfcommSocketToServiceRecord OK");
                        copy = theID.getUuid().toString();
                    } catch (IOException e) {
                        return null;
                    } catch (IndexOutOfBoundsException e) {
                        return null;
                    }
                } else {
                    try {
                        mmSocket   = InsecureBT.createRfcommSocketToServiceRecord(mmDevice, UUID.fromString(riderStr), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                try {
                    // Connect the device through the socket. This will block
                    // until it succeeds or throws an exception
                    mmSocket.connect();
                    Log.d("bt","connect OK");
                } catch (IOException connectException) {
                    Log.d("bt","Unable to connect");
                    Log.d("bt",connectException.getMessage());
                    // Unable to connect; close the socket and get out
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) { }
                    return null;
                }

                return mmSocket;
            }

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(MainActivity.this, "",
                        "Connecting. Please wait...", true);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(BluetoothSocket bluetoothSocket) {
                if (bluetoothSocket != null) {
                    if (wb!=null) {
                        wb.interrupt();
                    }
                    if (item.getTitle().toString().contains("BEEBA")) {
                        Toast.makeText(MainActivity.this,
                                "BEEBA connected",
                                Toast.LENGTH_SHORT).show();
                        wb=new VibroBrdDRV(bluetoothSocket);
                    } else if (item.getTitle().toString().contains("RIDER")) {
                        Toast.makeText(MainActivity.this,
                                "RIDER connected",
                                Toast.LENGTH_SHORT).show();
                        wb = new VibroBrd(bluetoothSocket);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Assuming firmware v2",
                                Toast.LENGTH_SHORT).show();
                        wb = new VibroBrdV2(bluetoothSocket);
                    }

                    wb.setListener((VibroBrd.WbBtListener) MainActivity.this);
                    wb.start();

                    setFragmentVisibility(true);
                    if (copy!=null) {
                        int sdk = Build.VERSION.SDK_INT;
                        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(copy);
                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip =
                                    ClipData.newPlainText("text label", copy);
                            clipboard.setPrimaryClip(clip);
                        }
                        Toast.makeText(MainActivity.this, copy, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this,
                            "Connection failed, please retry",
                            Toast.LENGTH_SHORT).show();
                    setFragmentVisibility(false);
                }
                dialog.dismiss();
            }
        }.execute();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void wbResponded(String msg) {
        if (msg.contains("E")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"Error reported",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void connectionLost() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Connection lost",Toast.LENGTH_SHORT).show();
                setFragmentVisibility(false);
            }
        });
    }
}
