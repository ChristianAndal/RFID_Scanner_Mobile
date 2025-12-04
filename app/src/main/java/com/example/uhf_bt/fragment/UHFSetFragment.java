package com.example.uhf_bt.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.R;
import com.example.uhf_bt.SPUtils;
import com.rscja.utility.LogUtility;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class UHFSetFragment extends Fragment implements View.OnClickListener {

    Button btnGetPower;
    Button btnSetPower;
    Spinner spPower;
    Spinner SpinnerMode;
    Button BtSetFre;
    Button BtGetFre;
    MainActivity context;
    RadioButton rbUsHop;
    RadioButton rbBRA;
    RadioButton rbOtherHop;
    Spinner spFreHop;
    Button btnSetFreHop;
    Button btnbeepOpen;
    Button btnbeepClose;
    CheckBox cbTagFocus;
    Button btnSetRFlink,btnGetRFlink;
    CheckBox cbRssi;

    private Spinner spProtocol;
    private Button btnSetProtocol;
    private Button btnGetProtocol;

    private RadioGroup rgWorkingMode;

    private CheckBox cbContinuousWave, cbAutoReconnect;

    LinearLayout llTidLen , llUserPtr , llUserLen,llTidPtr;
    EditText etTIDLen , etUserPtr , etUserLen,etTIDPtr;
    RadioButton rbEPCTIDUSER ,rbEPCTID, rbEPC,rbUnknown;
    Button btnSetInventory ,btnGetInventory;


    RadioButton rgLedOpen ,rgLedClose ,rgLedBlink  ;
    LinearLayout llLedP, llLedP1, llLedP2;
    Button btnLedSet,btnTriggerBeep;
    EditText etInterval ,etDuration ,etCount,etTime;

    LedOnClickListener ledOnClickListener=new LedOnClickListener();
    private String[] arrayPower;

    Spinner  spSessionID  ,spInventoried  ;
    Button btnSetSession  ,btnGetSession;

    private final static int GET_FRE = 1;
    private final static int GET_POWER = 2;
    private final static int GET_PROTOCOL = 3;
    private final static int GET_CW = 4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_FRE:
                    String mode = (String) msg.obj;
                    if (!mode.equals("")) {
                        String[] arrayMode = getResources().getStringArray(R.array.arrayMode);
                        for(int k=0;k<arrayMode.length;k++){
                            if(arrayMode[k].equals(mode)) {
                                SpinnerMode.setSelection(k);
                                break;
                            }
                        }
                    } else if (msg.arg1 == 1) {
                        context.showToast(R.string.uhf_msg_read_frequency_fail);
                    }
                    break;
                case GET_POWER:
                    int iPower = (int) msg.obj;
                    if(arrayPower != null && iPower > -1) {
                        for (int i = 0; i < arrayPower.length; i++) {
                            if (iPower == Integer.valueOf(arrayPower[i])) {
                                spPower.setSelection(i);
                                break;
                            }
                        }
                    } else if (msg.arg1 == 1) {
                        context.showToast(R.string.uhf_msg_read_power_fail);
                    }
                    break;
                case GET_PROTOCOL:
                    int pro = (int) msg.obj;
                    if (pro >= 0 && pro < spProtocol.getCount()) {
                        spProtocol.setSelection(pro);
                        if (msg.arg1 == 1)
                            context.showToast(R.string.uhf_msg_get_protocol_succ);
                    } else {
                        if (msg.arg1 == 1)
                            context.showToast(R.string.uhf_msg_get_protocol_fail);
                    }
                    break;
                case GET_CW:
                    int flag = (int) msg.obj;
                    if (flag == 1) {
                        cbContinuousWave.setChecked(true);
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_succ);
                    } else if (flag == 0) {
                        cbContinuousWave.setChecked(false);
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_succ);
                    } else {
                        if (msg.arg1 == 1)
                            context.showToast(R.string.get_fail);
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_uhfset, container, false);
        init(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (MainActivity) getActivity();
    }


    private void init(View view) {

        spSessionID= view.findViewById(R.id.spSessionID);
        spInventoried= view.findViewById(R.id.spInventoried);
        btnGetSession= view.findViewById(R.id.btnGetSession);
        btnSetSession= view.findViewById(R.id.btnSetSession);
        btnGetSession.setOnClickListener(this);
        btnSetSession.setOnClickListener(this);
        rgLedOpen = view.findViewById(R.id.rgLedOpen);
        rgLedClose = view.findViewById(R.id.rgLedClose);
        rgLedBlink = view.findViewById(R.id.rgLedBlink);
        llLedP = view.findViewById(R.id.llLedP);
        llLedP1 = view.findViewById(R.id.llLedP1);
        llLedP2 = view.findViewById(R.id.llLedP2);
        btnLedSet = view.findViewById(R.id.btnLedSet);
        btnTriggerBeep = view.findViewById(R.id.btnTriggerBeep);
        etInterval = view.findViewById(R.id.etInterval);
        etDuration = view.findViewById(R.id.etDuration);
        etCount = view.findViewById(R.id.etCount);
        etTime = view.findViewById(R.id.etTime);
        llLedP.setVisibility(View.GONE);
        llLedP1.setVisibility(View.GONE);
        llLedP2.setVisibility(View.GONE);

        rgLedOpen.setOnClickListener(ledOnClickListener);
        rgLedBlink.setOnClickListener(ledOnClickListener);
        rgLedClose.setOnClickListener(ledOnClickListener);
        btnLedSet.setOnClickListener(ledOnClickListener);
        btnTriggerBeep.setOnClickListener(new BuzzleOnClickListener());

        btnGetPower = (Button) view.findViewById(R.id.btnGetPower);
        btnSetPower = (Button) view.findViewById(R.id.btnSetPower);

        spPower = (Spinner) view.findViewById(R.id.spPower);
        arrayPower = getResources().getStringArray(R.array.arrayPower);
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, arrayPower);
        spPower.setAdapter(adapter);

        SpinnerMode = (Spinner) view.findViewById(R.id.SpinnerMode);
        BtSetFre = (Button) view.findViewById(R.id.BtSetFre);
        BtGetFre = (Button) view.findViewById(R.id.BtGetFre);

        rbUsHop = (RadioButton) view.findViewById(R.id.rbUsHop);
        rbBRA = (RadioButton) view.findViewById(R.id.rbBRA);
        rbOtherHop = (RadioButton) view.findViewById(R.id.rbOtherHop);
        spFreHop = (Spinner) view.findViewById(R.id.spFreHop);
        btnSetFreHop = (Button) view.findViewById(R.id.btnSetFreHop);

        btnbeepOpen = (Button) view.findViewById(R.id.btnbeepOpen);
        btnbeepClose = (Button) view.findViewById(R.id.btnbeepClose);
        cbTagFocus= (CheckBox) view.findViewById(R.id.cbTagFocus);
        cbRssi= (CheckBox) view.findViewById(R.id.cbRssi);
        btnSetRFlink=(Button)view.findViewById(R.id.btnSetRFlink);
        btnGetRFlink=(Button)view.findViewById(R.id.btnGetRFlink);
        llTidLen= view.findViewById(R.id.llTidLen);
        llUserPtr= view.findViewById(R.id.llUserPtr);
        llUserLen= view.findViewById(R.id.llUserLen);
        llTidPtr= view.findViewById(R.id.llTidPtr);
        etTIDLen= view.findViewById(R.id.etTIDLen);
        etUserPtr= view.findViewById(R.id.etUserPtr);
        etUserLen= view.findViewById(R.id.etUserLen);
        etTIDPtr= view.findViewById(R.id.etTIDPtr);
        llTidLen.setVisibility(View.GONE);
        llTidPtr.setVisibility(View.GONE);

        rbEPCTIDUSER= view.findViewById(R.id.rbEPCTIDUSER);
        rbEPCTID= view.findViewById(R.id.rbEPCTID);
        rbEPC= view.findViewById(R.id.rbEPC);
        rbUnknown= view.findViewById(R.id.rbUnknown);
        rbEPCTIDUSER.setOnClickListener(this);
        rbEPCTID.setOnClickListener(this);
        rbEPC.setOnClickListener(this);

        btnSetInventory= view.findViewById(R.id.btnSetInventory);
        btnGetInventory= view.findViewById(R.id.btnGetInventory);
        btnSetInventory.setOnClickListener(this);
        btnGetInventory.setOnClickListener(this);

        btnSetRFlink.setOnClickListener(this);
        btnGetRFlink.setOnClickListener(this);
        cbTagFocus.setOnClickListener(this);
        rbOtherHop.setOnClickListener(this);
        rbUsHop.setOnClickListener(this);
        rbBRA.setOnClickListener(this);
        btnSetFreHop.setOnClickListener(this);
        btnGetPower.setOnClickListener(this);
        btnSetPower.setOnClickListener(this);
        BtSetFre.setOnClickListener(this);
        BtGetFre.setOnClickListener(this);
        cbRssi.setOnClickListener(this);
        btnbeepOpen.setOnClickListener(this);
        btnbeepClose.setOnClickListener(this);
        getEpcTidUserMode();
        spProtocol = (Spinner) view.findViewById(R.id.spProtocol);
        btnSetProtocol = (Button) view.findViewById(R.id.btnSetProtocol);
        btnSetProtocol.setOnClickListener(this);
        btnGetProtocol = (Button) view.findViewById(R.id.btnGetProtocol);
        btnGetProtocol.setOnClickListener(this);


        cbContinuousWave = (CheckBox) view.findViewById(R.id.cbContinuousWave);
        cbContinuousWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = cbContinuousWave.isChecked() ? 1 : 0;
                setCW(flag, true);
            }
        });

        boolean reconnect = SPUtils.getInstance(getContext().getApplicationContext()).getSPBoolean(SPUtils.AUTO_RECONNECT, false);
        cbAutoReconnect = (CheckBox) view.findViewById(R.id.cbAutoReconnect);
        cbAutoReconnect.setChecked(reconnect);
        cbAutoReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.getInstance(getContext().getApplicationContext()).setSPBoolean(SPUtils.AUTO_RECONNECT, cbAutoReconnect.isChecked());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread() {
            @Override
            public void run() {
                getFre(false);
                getPower(false);
                getProtocol(false);
                getCW(false);
                getSession();

            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnGetPower) {
            getPower(true);
        } else if (id == R.id.btnSetPower) {
            setPower();
        } else if (id == R.id.BtGetFre) {
            getFre(true);
        } else if (id == R.id.BtSetFre) {
            setFre();
        } else if (id == R.id.btnSetFreHop) {
            setFre2();
        } else if (id == R.id.rbUsHop) {
            OnClick_rbUsHop();
        } else if (id == R.id.rbBRA) {
            OnClick_rbBRA();
        } else if (id == R.id.rbOtherHop) {
            OnClick_rbOtherHop();
        } else if (id == R.id.btnSetProtocol) {
            setProtocol();
        } else if (id == R.id.btnGetProtocol) {
            getProtocol(true);
        } else if (id == R.id.btnbeepClose) {
            if (context.uhf.setBeep(false)) {
                context.showToast(R.string.setting_succ);
            } else {
                context.showToast(R.string.setting_fail);
            }
        } else if (id == R.id.btnbeepOpen) {
            if (context.uhf.setBeep(true)) {
                context.showToast(R.string.setting_succ);
            } else {
                context.showToast(R.string.setting_fail);
            }
        } else if (id == R.id.cbTagFocus) {
            if (context.uhf.setTagFocus(cbTagFocus.isChecked())) {
                context.showToast(R.string.setting_succ);
            } else {
                context.showToast(R.string.setting_fail);
            }
        } else if (id == R.id.btnSetRFlink) {
            LogUtility.setDebug(true);
            if (context.uhf.setRFLink(1)){
                context.showToast("设置setRFLink(1)成功!");
            } else {
                context.showToast("设置setRFLink(1)失败!");
            }
        } else if (id == R.id.btnGetRFlink) {
            context.showToast("context.uhf.getRFLink()="+context.uhf.getRFLink());
        } else if (id == R.id.btnSetInventory) {
            setEpcTidUserMode();
        } else if (id == R.id.btnGetInventory) {
            getEpcTidUserMode();
        } else if (id == R.id.rbEPCTIDUSER) {
            llUserLen.setVisibility(View.VISIBLE);
            llUserPtr.setVisibility(View.VISIBLE);
        } else if (id == R.id.rbEPCTID) {
            llUserLen.setVisibility(View.GONE);
            llUserPtr.setVisibility(View.GONE);
        } else if (id == R.id.rbEPC) {
            llUserLen.setVisibility(View.GONE);
            llUserPtr.setVisibility(View.GONE);
        } else if (id == R.id.cbRssi) {
            if(cbRssi.isChecked()){
                context.isSupportRssi=true;
            }else{
                context.isSupportRssi=false;
            }
        } else if (id == R.id.btnGetSession) {
            if (getSession()) {
                context.showToast(R.string.get_succ);
            } else {
                context.showToast(R.string.get_fail);
            }
        } else if (id == R.id.btnSetSession) {
            setSession();
        }
    }

    private void sendMessage(int what, Object obj, int arg1) {
        Message msg = mHandler.obtainMessage(what, obj);
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }

    private void getPower(boolean showToast) {
        int iPower = context.uhf.getPower();
        sendMessage(GET_POWER, iPower, showToast ? 1 : 0);
    }

    private void setPower() {
        int iPower = Integer.valueOf(spPower.getSelectedItem().toString());
        if (context.uhf.setPower(iPower)) {
            Toast.makeText(context, R.string.uhf_msg_set_power_succ, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.uhf_msg_set_power_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public void getFre(boolean showToast) {
        int idx = context.uhf.getFrequencyMode();
        String mode="";
        switch (idx) {
            case 0x01:
                mode = getString(R.string.china_standard1);
                break;
            case 0x02:
                mode = getString(R.string.china_standard2);
                break;
            case 0x04:
                mode = getString(R.string.europe_standard);
                break;
            case 0x08:
                mode = getString(R.string.united_states_standard);
                break;
            case 0x016:
                mode = getString(R.string.korea);
                break;
            case 0x032:
                mode = getString(R.string.japan);
                break;
            case 0x033:
                mode = getString(R.string.South_Africa_915_919MHz);
                break;
            case 0x034:
                mode = getString(R.string.TAIWAN);
                break;
            case 0x035:
                mode = getString(R.string.vietnam_918_923MHz);
                break;
            case 0x036:
                mode = getString(R.string.Peru_915_928MHz);
                break;
            case 0x037:
                mode = getString(R.string.Russia_860_867MHZ);
                break;
            case 0x080:
                mode = getString(R.string.Morocco);
                break;
        }
        sendMessage(GET_FRE, mode, showToast ? 1 : 0);
    }

    private void setFre() {
        int f = 0;
        String mode=SpinnerMode.getSelectedItem().toString();
        if(getString(R.string.china_standard1).equals(mode)){
            f = 0x01;
        }else if(getString(R.string.china_standard2).equals(mode)){
            f = 0x02;
        }else if(getString(R.string.europe_standard).equals(mode)){
            f = 0x04;
        }else if(getString(R.string.united_states_standard).equals(mode)){
            f = 0x08;
        }else if(getString(R.string.korea).equals(mode)){
            f = 0x16;
        }else if(getString(R.string.japan).equals(mode)){
            f = 0x32;
        }else if(getString(R.string.South_Africa_915_919MHz).equals(mode)){
            f = 0x33;
        }else if(getString(R.string.TAIWAN).equals(mode)){
            f = 0x34;
        }else if(getString(R.string.vietnam_918_923MHz).equals(mode)){
            f = 0x35;
        }else if(getString(R.string.Peru_915_928MHz).equals(mode)){
            f = 0x36;
        }else if(getString(R.string.Russia_860_867MHZ).equals(mode)){
            f = 0x37;
        }else if(getString(R.string.Morocco).equals(mode)){
            f = 0x80;
        }

        if (context.uhf.setFrequencyMode(f)) {
            context.showToast(R.string.uhf_msg_set_frequency_succ);
        } else {
            context.showToast(R.string.uhf_msg_set_frequency_fail);
        }
    }

    private void setFre2() {
        if (context.uhf.setFreHop(new Float(spFreHop.getSelectedItem().toString().trim()).floatValue())) {
            context.showToast(R.string.uhf_msg_set_frequency_succ);
        } else {
            context.showToast(R.string.uhf_msg_set_frequency_fail);
        }
    }

    /**
     * 设置协议
     *
     * @return
     */
    private boolean setProtocol() {
        if (context.uhf.setProtocol(spProtocol.getSelectedItemPosition())) {
            context.showToast(R.string.uhf_msg_set_protocol_succ);
            return true;
        } else {
            context.showToast(R.string.uhf_msg_get_protocol_fail);
        }
        return false;
    }

    /**
     * 获取协议
     *
     * @param showToast
     * @return
     */
    private void getProtocol(boolean showToast) {
        int pro = context.uhf.getProtocol();
        sendMessage(GET_PROTOCOL, pro, showToast ? 1 : 0);
    }

    /**
     * 设置连续波
     *
     * @param flag
     * @param showToast
     */
    private void setCW(int flag, boolean showToast) {
        boolean res = context.uhf.setCW(flag);
        if (showToast) {
            if (res) {
                context.showToast(getString(R.string.setting_succ));
            } else {
                context.showToast(getString(R.string.setting_fail));
            }
        }
    }

    /**
     * 获取连续波
     *
     * @param showToast
     */
    private void getCW(boolean showToast) {
        int flag = context.uhf.getCW();
        sendMessage(GET_CW, flag, showToast ? 1 : 0);
    }


    public void OnClick_rbUsHop() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.arrayFreHop_us, android.R.layout.simple_spinner_item);
        spFreHop.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void OnClick_rbBRA() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.arrayFreHop_bra, android.R.layout.simple_spinner_item);
        spFreHop.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void OnClick_rbOtherHop() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(context, R.array.arrayFreHop, android.R.layout.simple_spinner_item);
        spFreHop.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    private void getEpcTidUserMode(){
        llUserPtr.setVisibility(View.GONE);
        llUserLen.setVisibility(View.GONE);
        rbUnknown.setChecked(true);
    }
    private void setEpcTidUserMode(){

        if(rbEPC.isChecked()){
            if(context.uhf.setEPCMode()){
                context.showToast(R.string.get_succ);
            }else{
                context.showToast(R.string.get_fail);
            }
        }else if(rbEPCTID.isChecked()){
            if(context.uhf.setEPCAndTIDMode()) {
                context.showToast(R.string.get_succ);
            }else{
                context.showToast(R.string.get_fail);
            }
        }else if (rbEPCTIDUSER.isChecked()){
            String strUserPtr=etUserPtr.getText().toString();
            String strUserLen=etUserLen.getText().toString();
            int userPtr=0;
            int userLen=6;
            if(!TextUtils.isEmpty(strUserPtr)){
                userPtr=Integer.parseInt(strUserPtr);
            }
            if(!TextUtils.isEmpty(strUserLen)){
                userLen=Integer.parseInt(strUserLen);
            }

            if(context.uhf.setEPCAndTIDUserMode( userPtr,userLen)) {
                context.showToast(R.string.get_succ);
            }else{
                context.showToast(R.string.get_fail);
            }
        }

    }
    class LedOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
              int id = v.getId();
              if (id == R.id.rgLedOpen) {
                  llLedP.setVisibility(View.GONE);
                  llLedP1.setVisibility(View.GONE);
                  llLedP2.setVisibility(View.GONE);
              } else if (id == R.id.rgLedClose) {
                  llLedP.setVisibility(View.GONE);
                  llLedP1.setVisibility(View.GONE);
                  llLedP2.setVisibility(View.GONE);
              } else if (id == R.id.rgLedBlink) {
                  llLedP.setVisibility(View.VISIBLE);
                  llLedP1.setVisibility(View.VISIBLE);
                  llLedP2.setVisibility(View.VISIBLE);
              } else if (id == R.id.btnLedSet) {
                  if(rgLedBlink.isChecked()){
                      int duration=Integer.parseInt(etDuration.getText().toString());
                      int interval=Integer.parseInt(etInterval.getText().toString());
                      int count=Integer.parseInt(etCount.getText().toString());
                      context.uhf.blinkOfLed(duration,interval,count);
                      context.showToast("ok");
                      }else if(rgLedOpen.isChecked()){
                          context.uhf.openLed();
                          context.showToast("ok");
                      }else if(rgLedClose.isChecked()){
                          context.uhf.closeLed();
                          context.showToast("ok");
                      }else{
                          context.showToast(R.string.psam_msg_fail);
                      }
              }
        }
    }
    private boolean getSession() {
        char[] p = context.uhf.getGen2();
        if (p != null && p.length >= 14) {
            int target = p[0];
            int action = p[1];
            int t = p[2];
            int q = p[3];
            int startQ = p[4];
            int minQ = p[5];
            int maxQ = p[6];
            int dr = p[7];
            int coding = p[8];
            int p1 = p[9];
            int Sel = p[10];
            final int Session = p[11];
            final int g = p[12];
            final int linkFrequency = p[13];
            StringBuilder sb = new StringBuilder();
            sb.append("target=");
            sb.append(target);
            sb.append(" ,action=");
            sb.append(action);
            sb.append(" ,t=");
            sb.append(t);
            sb.append(" ,q=");
            sb.append(q);
            sb.append(" startQ=");
            sb.append(startQ);
            sb.append(" minQ=");
            sb.append(minQ);
            sb.append(" maxQ=");
            sb.append(maxQ);
            sb.append(" dr=");
            sb.append(dr);
            sb.append(" coding=");
            sb.append(coding);
            sb.append(" p=");
            sb.append(p1);
            sb.append(" Sel=");
            sb.append(Sel);
            sb.append(" Session=");
            sb.append(Session);
            sb.append(" g=");
            sb.append(g);
            sb.append(" linkFrequency=");
            sb.append(linkFrequency);
            //Log.i(TAG, sb.toString());
             context.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     spSessionID.setSelection(Session);
                     spInventoried.setSelection(linkFrequency==1?2:g);
                 }
             });
            return true;
        }
        return false;
    }
    private void setSession(){
        int seesionid = spSessionID.getSelectedItemPosition();
        int inventoried = spInventoried.getSelectedItemPosition();
        if (seesionid < 0 || inventoried < 0) {
            return;
        }
        char[] p = context.uhf.getGen2();
        if (p != null && p.length >= 14) {
            int target = p[0];
            int action = p[1];
            int t = p[2];
            int q = p[3];
            int startQ = p[4];
            int minQ = p[5];
            int maxQ = p[6];
            int dr = p[7];
            int coding = p[8];
            int p1 = p[9];
            int Sel = p[10];
            int Session = p[11];
            int g = p[12];
            int linkFrequency = p[13];
            StringBuilder sb = new StringBuilder();
            sb.append("target=");
            sb.append(target);
            sb.append(" ,action=");
            sb.append(action);
            sb.append(" ,t=");
            sb.append(t);
            sb.append(" ,q=");
            sb.append(q);
            sb.append(" startQ=");
            sb.append(startQ);
            sb.append(" minQ=");
            sb.append(minQ);
            sb.append(" maxQ=");
            sb.append(maxQ);
            sb.append(" dr=");
            sb.append(dr);
            sb.append(" coding=");
            sb.append(coding);
            sb.append(" p=");
            sb.append(p1);
            sb.append(" Sel=");
            sb.append(Sel);
            sb.append(" Session=");
            sb.append(Session);
            sb.append(" g=");
            sb.append(g);
            sb.append(" linkFrequency=");
            sb.append(linkFrequency);
            sb.append("seesionid=");
            sb.append(seesionid);
            sb.append(" inventoried=");
            sb.append(inventoried);
            //Log.i(TAG, sb.toString());
            if(inventoried==2) {
                linkFrequency = 1;
                inventoried=g;
            }else {
                linkFrequency = 0;
            }
            if (context.uhf.setGen2(target, action, t, q, startQ, minQ, maxQ, dr, coding, p1, Sel, seesionid, inventoried, linkFrequency)) {
                context.showToast(R.string.get_succ);
            } else {
                context.showToast(R.string.get_fail);
            }
        } else {
            context.showToast(R.string.get_fail);
        }
    }

    class BuzzleOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.btnTriggerBeep) {
                int time=Integer.parseInt(etTime.getText().toString());
                context.uhf.triggerBeep(time);
                context.showToast("ok");
            }
        }
    }
}
