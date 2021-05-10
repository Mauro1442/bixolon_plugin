package com.partsilicon.bixolon_printer_plugin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bxl.config.editor.BXLConfigLoader;
import com.partsilicon.bixolon_printer_plugin.PrinterControl.BixolonPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static com.partsilicon.bixolon_printer_plugin.UtilsKt.textToBitmap;
import static com.partsilicon.eliteutils.QrCodeUtilKt.generateQRCode;

public class PrinterActivity extends AppCompatActivity
        implements View.OnClickListener ,  View.OnTouchListener {

    private static BixolonPrinter bxlPrinter = null;

    private final int REQUEST_PERMISSION = 0;
    private final String DEVICE_ADDRESS_START = " (";
    private final String DEVICE_ADDRESS_END = ")";

    private final ArrayList<CharSequence> bondedDevices = new ArrayList<>();
    private ArrayAdapter<CharSequence> arrayAdapter;

    private int portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH;
    private String logicalName = "";
    private String address = "";

    private TextView tvAddress;
    private Spinner spinnerPairedDevices; //list bluetooth devices
    private boolean checkBoxAsyncMode = true;

    private Button btnPrinterOpen;
    private Button btnTestPrint;

    private ProgressBar mProgressLarge;

    String text_to_print = "";
    String qrCode_to_print = "";
    Settings settings;

    String connect_fail = "پرینتر یافت نشد\nتنظیمات پرینتر را بررسی نمایید";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        //
        settings = new Settings(this);
        text_to_print = getIntent().getStringExtra("text");
        qrCode_to_print = getIntent().getStringExtra("qrCode");

        final int ANDROID_NOUGAT = 24;
        if(Build.VERSION.SDK_INT >= ANDROID_NOUGAT)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        bxlPrinter = new BixolonPrinter(getApplicationContext());

        //Printer connect activity
        btnPrinterOpen = findViewById(R.id.btnPrinterOpen);
        btnPrinterOpen.setOnClickListener(this);
        btnTestPrint = findViewById(R.id.btnTestPrint);
        btnTestPrint.setOnClickListener(this);

        tvAddress = findViewById(R.id.tvAddress);
        mProgressLarge = findViewById(R.id.progressBar);
        mProgressLarge.setVisibility(ProgressBar.GONE);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bondedDevices);
        spinnerPairedDevices = findViewById(R.id.spinnerPairedDevices);
        spinnerPairedDevices.setAdapter(arrayAdapter);
        spinnerPairedDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String device = ((TextView) view).getText().toString();
                address = device.substring(device.indexOf(DEVICE_ADDRESS_START) + DEVICE_ADDRESS_START.length(), device.indexOf(DEVICE_ADDRESS_END));
                tvAddress.setText(device);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        setPairedDevices();

        Spinner modelList = findViewById(R.id.spinnerModelList);

        ArrayAdapter modelAdapter = ArrayAdapter.createFromResource(this, R.array.modelList, android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelList.setAdapter(modelAdapter);
        modelList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                logicalName = (String) parent.getItemAtPosition(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSION);
        //Load from settings
        if(!settings.getDeviceName().isEmpty()) {
            int index = Arrays.asList( getResources().getStringArray(R.array.modelList)).indexOf(settings.getDeviceName());
            if(index>=0)
                modelList.setSelection(index , true);
            else
                Log.d("SGS","index not found");
        }
        //launched for print
        if(text_to_print!=null && !text_to_print.isEmpty()){
            final String devAddr = settings.getDeviceAddress();
            final String devName = settings.getDeviceName();
            if(devAddr.isEmpty())
            {
                Toast.makeText(this , "Please select the printer",Toast.LENGTH_LONG).show();
            }else {
                connectPrinter(devName , devAddr , true);
            }
        }
    }

    void connectPrinter(final String devName, final String devAddr, final boolean openAndPrint){
        mProgressLarge.setVisibility(ProgressBar.VISIBLE);
        btnPrinterOpen.setVisibility(View.INVISIBLE);
        //open device
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bxlPrinter.printerOpen(portType, devName, devAddr, checkBoxAsyncMode)) {
                    if(openAndPrint)
                        printAsImage();
                    else
                    {
                        settings.saveDeviceName(logicalName);
                        settings.saveDeviceAddress(address);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressLarge.setVisibility(ProgressBar.GONE);
                            Toast.makeText(PrinterActivity.this , "پرینتر متصل شد",Toast.LENGTH_LONG).show();
                            if(openAndPrint)
                                finish();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressLarge.setVisibility(ProgressBar.GONE);
                            btnPrinterOpen.setVisibility(View.VISIBLE);
                            Toast.makeText(PrinterActivity.this , connect_fail,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bxlPrinter.printerClose();
    }

    private void setPairedDevices() {
        bondedDevices.clear();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter.getBondedDevices();

        String item = "";
        int savedPos = -1;
        if(!settings.getDeviceAddress().isEmpty()) {
            item = settings.getDeviceName() + DEVICE_ADDRESS_START + settings.getDeviceAddress() + DEVICE_ADDRESS_END;
        }

        int i =0;
        for (BluetoothDevice device : bondedDeviceSet) {
            String tmpItem = device.getName() + DEVICE_ADDRESS_START + device.getAddress() + DEVICE_ADDRESS_END;
            bondedDevices.add(tmpItem);
            if(!item.isEmpty() && item.equals(tmpItem))
                savedPos = i;
            i++;
        }

        if (arrayAdapter != null) {
            arrayAdapter.notifyDataSetChanged();
        }
        if(savedPos >= 0)
            spinnerPairedDevices.setSelection(savedPos);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            spinnerPairedDevices.requestDisallowInterceptTouchEvent(false);
        else spinnerPairedDevices.requestDisallowInterceptTouchEvent(true);
        return false;
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnPrinterOpen) {
            connectPrinter(logicalName , address ,false);
        }else if(view.getId() == R.id.btnTestPrint){
            text_to_print = "برای تست وضعیت اتصال پرینتر\n";
            printAsImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /*public final Handler mHandler = new Handler(new Handler.Callback() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:

                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    break;
                case 1:
                    String data = (String) msg.obj;
                    if (data != null && data.length() > 0) {
                        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                    }
                    mProgressLarge.setVisibility(ProgressBar.GONE);
                    //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    break;
                case 2:
                    finish();
                    break;
            }
            return false;
        }
    });
*/
    void printAsImage(){
        Bitmap bmp = textToBitmap(text_to_print);
        if(bmp == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PrinterActivity.this , "خطا در آماده سازی چاپ" , Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        bxlPrinter.printImage(bmp,380,bxlPrinter.ALIGNMENT_CENTER,0,0,0);
        if( qrCode_to_print!=null && !qrCode_to_print.isEmpty()) {
            bxlPrinter.printImage(generateQRCode(qrCode_to_print),280,bxlPrinter.ALIGNMENT_CENTER,0,0,0);
            //Not worked
            /*int symbology = bxlPrinter.BARCODE_TYPE_QRCODE;
            int Hri = bxlPrinter.BARCODE_HRI_NONE;
            bxlPrinter.printBarcode(qrCode_to_print, symbology, 1, 280, bxlPrinter.ALIGNMENT_CENTER, Hri);*/
        }
        bxlPrinter.printText(" ", 0, 0,0);//extra line at end
    }

    /*void print(String strData ){

        int alignment, attribute = 0;

        switch(1)
        {
            case 0:		alignment = bxlPrinter.ALIGNMENT_LEFT;		break;
            case 1:		alignment = bxlPrinter.ALIGNMENT_CENTER;	    break;
            case 2:		alignment = bxlPrinter.ALIGNMENT_RIGHT;		break;
            default:	alignment = bxlPrinter.ALIGNMENT_LEFT;		break;
        }

        switch(0)
        {
            case 0:		attribute |= bxlPrinter.ATTRIBUTE_FONT_A;	break;
            case 1:		attribute |= bxlPrinter.ATTRIBUTE_FONT_B;	break;
            case 2:		attribute |= bxlPrinter.ATTRIBUTE_FONT_C;	break;
            *//*case 3:		attribute |= MainActivity.bxlPrinter.ATTRIBUTE_FONT_D;	break;*//*
            default:	attribute |= bxlPrinter.ATTRIBUTE_FONT_A;	break;
        }

        *//*if(checkBold.isChecked())
        {
            attribute |= MainActivity.bxlPrinter.ATTRIBUTE_BOLD;
        }*//*

        *//*if(checkUnderline.isChecked())
        {
            attribute |= MainActivity.bxlPrinter.ATTRIBUTE_UNDERLINE;
        }

        if(checkReverse.isChecked())
        {
            attribute |= MainActivity.bxlPrinter.ATTRIBUTE_REVERSE;
        }*//*
        bxlPrinter.setCharacterSet(BixolonPrinter.CS_FARSI);
        bxlPrinter.setFarsiOption(BixolonPrinter.OPT_REORDER_FARSI_RTL);
        int spinnerSize = 0;
        bxlPrinter.printText(strData, alignment, attribute, (spinnerSize + 1));

        if( qrCode_to_print!=null && !qrCode_to_print.isEmpty()) {
            int symbology = bxlPrinter.BARCODE_TYPE_QRCODE;
            int Hri = bxlPrinter.BARCODE_HRI_NONE;
            bxlPrinter.printBarcode(qrCode_to_print, symbology, 280, 280, alignment, Hri);
        }
    }*/
}