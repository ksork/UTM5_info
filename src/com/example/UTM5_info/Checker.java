package com.example.UTM5_info;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Checker {

    public static boolean cabAvailable(Context context){return !cabUnavailable(context);}

    public static boolean cabUnavailable(Context context){
        CabChecker checker = new CabChecker();
        checker.execute();
        try {
            if(checker.get()){
                return false;
            }
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
        Toast.makeText(context, "Сервис недоступен", Toast.LENGTH_SHORT).show();
        return true;
    }

    static class CabChecker extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Jsoup.connect(User.CAB_URL).get();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
