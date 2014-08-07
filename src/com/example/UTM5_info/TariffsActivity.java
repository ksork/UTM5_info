package com.example.UTM5_info;

import android.app.Activity;
import android.content.Context;
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
    private ArrayList tariffData;
    private ListView lvTariffs;
    private Context context;
    private final String[] TARIFFS_ID = {"8", "7", "9", "10"};
    private final int[] TARIFFS_PRICE = {400, 500, 600, 800};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tariffs_layout);

        context = getApplicationContext();
        lvTariffs = (ListView) findViewById(R.id.lvTariffs);
        tariffData = MainActivity.user.getTariffList();
        // Меняем тарифы "Оптима" и "Актив" местами
        Collections.swap(tariffData, 1, 2);
        showTariffs();
        }

    // Выводим список тарифов на экран
    private void showTariffs(){
        String[] from = {"tariffName", "tariffDescription", "tariffEnable"};
        int[] to = {R.id.tvTariffName, R.id.tvTariffDescription, R.id.imgGalka};
        SimpleAdapter adapter = new SimpleAdapter(this, tariffData, R.layout.tariffs_list_item, from, to);
        lvTariffs.setAdapter(adapter);
        lvTariffs.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    // Проверка количества бабла, нужного для перехода на выбраный тариф
    private boolean isBalanceLow(int tariffSelected){
        int balance = Integer.parseInt(MainActivity.user.getBalance());
        int tariffPrice = TARIFFS_PRICE[tariffSelected]+1;
        return (balance < tariffPrice);
    }

    public void onBtnOkClick(View v){
        // Проверяем соединение с кабинетом
        if (Checker.cabUnAvailable(context)){
            setResult(1);
            return;
        }
        int tariffSelected = lvTariffs.getCheckedItemPosition();
        if (tariffSelected == -1){return;}
        // Проверяем баланс
        if (isBalanceLow(tariffSelected)){
            Dialog.showMessage(TariffsActivity.this, "Недостаточно денег !", "Для перехода на выбраный тариф " +
                    "необходим баланс более " + TARIFFS_PRICE[tariffSelected] + "руб.");
            return;
        }
        MainActivity.user.changeTariff(TARIFFS_ID[tariffSelected]);
        setResult(RESULT_OK);
        finish();
    }
}
