package com.example.UTM5_info;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by k on 29.07.14.
 */
public class MainActivity extends Activity {
    public static User user;
    private final String LOG_TAG = "Main_log";
    private final String PREFS_NAME = "PrefsFile";
    private final int REQUEST_CODE_SETTINGS = 1;
    private final int REQUEST_CODE_TARIFFS = 2;
    private final int REQUEST_CODE_ADD_BLOCK = 3;
    private final int REQUEST_CODE_GOTO_SITE = 4;
    private final Context CONTEXT = MainActivity.this;
    private String userLogin;
    private String userPassword;

    private TextView tvLogin;
    private TextView tvAccountId;
    private TextView tvCurrentTariff;
    private TextView tvBalance;
    private TextView tvDaysLeft;
    private TextView tvDayEnding;
    private Button btnAddCredit;
    private Button btnChangeTariff;
    private Button btnAddBlock;
    private Button btnSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvAccountId = (TextView) findViewById(R.id.tvAccountId);
        tvCurrentTariff = (TextView) findViewById(R.id.tvCurrentTariff);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        tvDaysLeft = (TextView) findViewById(R.id.tvDaysLeft);
        tvDayEnding = (TextView) findViewById(R.id.tvDayEnding);
        btnAddCredit = (Button) findViewById(R.id.btnAddCredit);
        btnChangeTariff = (Button) findViewById(R.id.btnChangeTariff);
        btnAddBlock = (Button) findViewById(R.id.btnAddBlock);
        btnSettings = (Button) findViewById(R.id.btnSettings);

        loadAccountData();
        refreshData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SETTINGS: // Возврат из settings_activity
                    userLogin = data.getStringExtra("userLogin");
                    userPassword = data.getStringExtra("userPassword");
                    user = new User(userLogin, userPassword);
                    saveAccountData();
                    refreshData();
                    break;
                case REQUEST_CODE_TARIFFS:
                    break;
                case REQUEST_CODE_ADD_BLOCK:
                    break;
                case REQUEST_CODE_GOTO_SITE:
                    break;
            }
        }
    }

    // Загрузка ранее сохраненных логина/пароля
    protected void loadAccountData() {
        SharedPreferences data = getSharedPreferences(PREFS_NAME, 0);
        userLogin = data.getString("userLogin", "-----");
        userPassword = data.getString("userPassword", "-----");
    }

    // Сохранение логина/пароля
    protected void saveAccountData() {
        SharedPreferences data = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = data.edit();
        editor.putString("userLogin", userLogin);
        editor.putString("userPassword", userPassword);
        editor.commit();
    }

    // Выкл все кнопки
    protected void disableButtons() {
        btnSettings.setEnabled(false);
        btnAddBlock.setEnabled(false);
        btnAddCredit.setEnabled(false);
        btnChangeTariff.setEnabled(false);
    }

    // Вкл все кнопки
    protected void enableButtons() {
        btnSettings.setEnabled(true);
        btnAddBlock.setEnabled(true);
        btnAddCredit.setEnabled(true);
        btnChangeTariff.setEnabled(true);
    }

    // Получаем данные с сайта, выводим на экран
    protected void refreshData() {
        if (Checker.cabUnavailable(CONTEXT)) { //нет связи
            tvLogin.setText(userLogin);
            tvAccountId.setText("-----");
            tvCurrentTariff.setText("-----");
            tvBalance.setText("0");
            tvDaysLeft.setText("0");
            tvDayEnding.setText("дней");
            return;
        }
        user = new User(userLogin, userPassword);
        if (!user.isLoginOk()) {              //связь есть, но логин/пароль не верны
            tvLogin.setText(userLogin);
            tvAccountId.setText("-----");
            tvCurrentTariff.setText("-----");
            tvBalance.setText("0");
            tvDaysLeft.setText("0");
            tvDayEnding.setText("дней");
        } else {                             //все нормально
            tvLogin.setText(userLogin);
            tvAccountId.setText(user.getAccountId());
            tvCurrentTariff.setText(user.getCurrentTariffName());
            tvBalance.setText(user.getBalance());
            tvDaysLeft.setText(user.getDaysLeft());
            tvDayEnding.setText(Day.getNumEnding(user.getDaysLeft()));
        }
    }

    // Кнопка настройки
    public void onClickBtnSettings(View v) {
        if (Checker.cabAvailable(CONTEXT)) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("userLogin", userLogin);
            intent.putExtra("userPassword", userPassword);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        }
    }

    // Кнопка "Сменить тариф"
    public void onClickBtnChangeTariff(View v) {
        if (Checker.cabAvailable(CONTEXT)) {
            Intent intent = new Intent(this, TariffsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_TARIFFS);
        }
    }

    // Кнопка "блокировка счёта"
    public void onClickBtnAddBlock(View v) {
        Dialog.showMessage(CONTEXT, "Блокировка", "До этого не дошли руки");
    }

    // Кнопка "Взять кредит"
    public void onClickBtnAddCredit(View v) {
        disableButtons();
        if (Checker.cabAvailable(CONTEXT)) {
            user = new User(userLogin, userPassword);
            if (Integer.parseInt(user.getBalance()) > 0) {
                Dialog.showMessage(CONTEXT, "Кредит",
                        "Услуга доступна при отрицательном балансе");
                enableButtons();
                return;
            }
            String resultOfCredit = user.addCredit();
            if (resultOfCredit == null) {
                Dialog.showMessage(CONTEXT, "Кредит",
                        "Кредит установлен, срок действия 5 дней");
                enableButtons();
                return;
            }
            Dialog.showMessage(CONTEXT, "Кредит",
                    "Вы использовали кредит " + resultOfCredit + ", услуга доступна раз в 30 дней");
        }
        enableButtons();
    }
}
