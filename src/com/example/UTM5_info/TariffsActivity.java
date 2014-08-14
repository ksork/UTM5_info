package com.example.UTM5_info;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by k on 28.07.14.
 */
public class TariffsActivity extends Activity{

    private final String LOG_TAG = "Tariff_log";
    private Context context;
    private final String[] TARIFFS_ID = {"8", "7", "9", "10"};
    private final int[] TARIFFS_PRICE = {400, 500, 600, 800};
    private ArrayList tariffData;
    private  CheckBox[] checkBoxes = new CheckBox[4];

    private ListView lvTariffs;
    private CheckBox cbTariff1;
    private CheckBox cbTariff2;
    private CheckBox cbTariff3;
    private CheckBox cbTariff4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tariffs_layout);

        context = getApplicationContext();
        lvTariffs = (ListView) findViewById(R.id.lvTariffs);
        cbTariff1 = (CheckBox) findViewById(R.id.cbTariff1);
        cbTariff2 = (CheckBox) findViewById(R.id.cbTariff2);
        cbTariff3 = (CheckBox) findViewById(R.id.cbTariff3);
        cbTariff4 = (CheckBox) findViewById(R.id.cbTariff4);
        checkBoxes[0] = cbTariff1;
        checkBoxes[1] = cbTariff2;
        checkBoxes[2] = cbTariff3;
        checkBoxes[3] = cbTariff4;

        tariffData = MainActivity.user.getTariffList();
        // Меняем тарифы "Оптима" и "Актив" местами
        Collections.swap(tariffData, 1, 2);
        }


    // Проверка количества бабла, нужного для перехода на выбраный тариф
    private boolean isBalanceLow(int tariffSelect){
        int balance = Integer.parseInt(MainActivity.user.getBalance());
        int tariffPrice = TARIFFS_PRICE[tariffSelect];
        return (balance < tariffPrice);
    }

    public void onBtnOkClick(View v){
        // Проверяем соединение с кабинетом
        if (Checker.cabUnAvailable(context)){
            setResult(1);
            return;
        }

        int tariffSelect;
        if(cbTariff1.isChecked()) tariffSelect = 0;
        else if(cbTariff2.isChecked()) tariffSelect = 1;
        else if(cbTariff3.isChecked()) tariffSelect = 2;
        else if(cbTariff4.isChecked()) tariffSelect = 3;
        else return;
        // Проверяем баланс
        if (isBalanceLow(tariffSelect)){
            Dialog.showMessage(TariffsActivity.this, "Недостаточно денег !", "Для перехода на выбраный тариф " +
                    "необходим баланс более " + TARIFFS_PRICE[tariffSelect] + "руб.");
            return;
        }
        MainActivity.user.changeTariff(TARIFFS_ID[tariffSelect]);
        setResult(RESULT_OK);
        finish();
    }

    // Снимаем галочки с не выбранных чекбоксов
    public void onCbClick(View v) {
        for (CheckBox cb : checkBoxes) {
            if (cb.getId() != v.getId()) cb.setChecked(false);
        }
    }
}
