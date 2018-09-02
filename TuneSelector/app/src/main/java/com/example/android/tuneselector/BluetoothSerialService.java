package com.example.android.tuneselector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothSerialService {
    private static final UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //This must match the server on RPi
    private final BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public BluetoothSerialService(Context context){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    public synchronized int getState(){
        return mState;
    }
    public synchronized  void start(){
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public synchronized void stop(){
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mState = STATE_NONE;
    }
    public synchronized void connect(BluetoothDevice device){
        if (mState == STATE_CONNECTING){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public synchronized void connected(BluetoothSocket socket){

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    public void write(byte[] out){
        ConnectedThread r;
        synchronized (this){
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread (BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket tmp = null;
            try{
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e){
                System.out.println("Failure to create RFCOMM with uuid" + e.getMessage());
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run(){
            mBluetoothAdapter.cancelDiscovery();
            try{
                mmSocket.connect();
            } catch (IOException e){
                System.out.println("Failure to connect to socket: "+e.getMessage());
                try{
                    mmSocket.close();
                } catch (IOException e2){
                    System.out.println("Failure to close socket: "+e.getMessage());
                }
                return;
            }
            synchronized (BluetoothSerialService.this){
                mConnectThread = null;
            }
            connected(mmSocket);
        }

        public void cancel() {
            try{
                mmSocket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            OutputStream tmpOut = null;

            try{
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }
            mmOutputStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run(){

        }

        public void write(byte[] buffer){
            try{
                mmOutputStream.write(buffer);
            } catch (IOException e){
                System.out.println("Failure writing to buffer: "+e.getMessage());
            }

        }

        public void cancel() {
            try{
                mmSocket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
