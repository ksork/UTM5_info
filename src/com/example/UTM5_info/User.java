package com.example.UTM5_info;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by k on 25.07.14.
 */
public class User {
    private final String LOG_TAG = "User_log";
    public final static String CAB_URL="http://cab.inettel.ru";
    private final String IMG_CHECKED = String.valueOf(R.drawable.galochka64x64);
    private String userLogin;
    private String userPassword;
    private Document mainHtml;
    private boolean syncInProgress = false;
    String sttr;


    // Конструктор
    public User(String userLogin, String userPassword){
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        this.refreshMainHtml();
    }

    // Проверка коректности логина/пароля
    public boolean isLoginOk(){
        String data = mainHtml.select("table").text();
        if (data.length() != 0){
            return true;
        }
        return false;
    }

    // Лицевой счет
    public String getAccountId(){
        return grabTable(mainHtml, 0, 1);
    }

    // Баланс
    public String getBalance(){
        String[] data = grabTable(mainHtml, 9, 1).split(" ");
        float num = Float.parseFloat(data[0]);
        int in = (int) (num+0.5f);
        return String.valueOf(in);
    }

    // Стоимость суток
    public String getDayPrice(){
        return grabTable(mainHtml, 14, 5);
    }

    // Остаток дней
    public String getDaysLeft() {
        float balance = Float.parseFloat(getBalance());
        int price = Integer.parseInt(getDayPrice());
        int daysLeft = (int) (balance + 0.5) / price;
        return String.valueOf(daysLeft);
    }

    // Текущий тариф
    public String getCurrentTariffName(){
        return  grabTable(mainHtml, 8, 0);
    }

    // Следующий тариф
    public String getNextTariffName(){
        return grabTable(mainHtml, 8, 1);
    }

    // Берем кредит если возможно, или возвращаем дату последнего кредита
    public String addCredit(){
        String result;
        CreditAdder creditAdder = new CreditAdder();
        creditAdder.execute();
        try {
            result = creditAdder.get();
        }catch (InterruptedException e){
            e.printStackTrace();
            result = null;
        }catch (ExecutionException e){
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    // Логинимся на сайт, получаем главную старничку кабинета
    public void refreshMainHtml(){
        MainHtmlRefresher refresher = new MainHtmlRefresher();
        refresher.execute();
        try {
            mainHtml = refresher.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Список доступных тарифов
    public ArrayList<HashMap<String, String>> getTariffList(){
        GetTariffList task = new GetTariffList();
        task.execute();
        ArrayList tariffList = new ArrayList();
        try {
            Document tariffHtml = task.get();
            Elements tariffLabels = tariffHtml.select("label");
            Elements tariffData = tariffHtml.select("table");

            for(int i = 0; i<tariffLabels.size()-1; i++ ) {
                HashMap<String, String> tariff = new HashMap<String, String>();
                String tariffName = tariffLabels.get(i).select("span").text();
                String tariffDescription = tariffLabels.get(i).text().split(" ", 2)[1];
                String tariffPrice = tariffData.get(i + 1).select("td").get(3).text();
                tariff.put("tariffName", tariffName);
                tariff.put("tariffDescription", tariffDescription);
                tariff.put("tariffPrice", tariffPrice);

                if(tariffName.equals(getCurrentTariffName())){
                    tariff.put("tariffEnable", IMG_CHECKED);
                }else{
                    tariff.put("tariffEnable", null);
                }
                tariffList.add(tariff);
            }
        } catch (InterruptedException e) {e.printStackTrace();
        } catch (ExecutionException e) {e.printStackTrace();}
        return tariffList;
    }

    // Смена тарифа
    public void changeTariff(String nextTariffId){
        ChangeTariff task = new ChangeTariff();
        task.execute(nextTariffId);
    }

    // Ожидание получения данных с сайта
    private void waitIfSync(){
        while (syncInProgress) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Парсер таблиц
    private String grabTable(Document html, int row, int column){
        Element rowData = html.select("tr").get(row);
        Element columnData = rowData.select("td").get(column);
        return columnData.text();
    }

    // Логинимся на сайт, получаем главную старничку кабинета
    class MainHtmlRefresher extends AsyncTask<Void, Void, Document> {

        @Override
        protected Document doInBackground(Void... voids) {
            Document html;
            try {
                html = Jsoup.connect(CAB_URL)
                        .data("bootstrap[username]", userLogin)
                        .data("bootstrap[password]", userPassword)
                        .post();
            }
            catch (IOException e){
                syncInProgress = false;
                return null;
            }
            syncInProgress = false;
            return html;
        }
    }

    // Кредит
    class CreditAdder extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try { // Логинимся в кабинет, сохраняем куки
                Connection.Response res = Jsoup.connect(CAB_URL)
                        .data("bootstrap[username]", userLogin)
                        .data("bootstrap[password]", userPassword)
                        .method(Connection.Method.POST).execute();
                Map<String, String> sessionId = res.cookies();
                // Переход на страничку кредита
                Document doc = Jsoup.connect(CAB_URL+"/user/promise-payment/aid/"+getAccountId())
                        .cookies(sessionId).get();
                if(doc.select("form").text().length() == 0){
                    Log.d(LOG_TAG, "Credit unavailable");
                    return grabTable(doc, 0, 1).split(" ")[0];
                }else{
                    // Берём кредит
                    Jsoup.connect(CAB_URL+"/user/promise-payment/aid/"+getAccountId())
                            .data("credit_sum", "500")
                            .data("accepted", "1")
                            .cookies(sessionId).post();
                    // Вкл. интернет
                    Jsoup.connect(CAB_URL+"/user/change-status/int_status/1/aid/"+getAccountId())
                            .cookies(sessionId).get();
                }
            }
            catch (IOException e){}
            return null;
        }

    }

    // Список доступных тарифов
    class GetTariffList extends AsyncTask<Void, Void, Document>{

        @Override
        protected Document doInBackground(Void... params) {
            try { // Логинимся в кабинет, сохраняем куки
                Connection.Response res = Jsoup.connect(CAB_URL)
                        .data("bootstrap[username]", userLogin)
                        .data("bootstrap[password]", userPassword)
                        .method(Connection.Method.POST).execute();
                Map<String, String> sessionId = res.cookies();
                // Переход на страничку смены тарифа
                Document doc = Jsoup.connect(CAB_URL+"/user/change-tariff/").cookies(sessionId).get();
                return doc;
            }catch (IOException e){return null;}
        }
    }

    // Смена тарифа
    class ChangeTariff extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... nextTariff) {
            try { // Логинимся в кабинет, сохраняем куки
                Connection.Response res = Jsoup.connect(CAB_URL)
                        .data("bootstrap[username]", userLogin)
                        .data("bootstrap[password]", userPassword)
                        .method(Connection.Method.POST).execute();
                Map<String, String> sessionId = res.cookies();
                // Переход на страничку смены тарифа
                Jsoup.connect(CAB_URL+"/user/change-tariff/")
                        .data("next_tp", nextTariff[0])
                        .data("accepted", "1")
                        .cookies(sessionId)
                        .method(Connection.Method.POST).execute();
                return null;
            }catch (IOException e){return null;}
        }
    }
}
