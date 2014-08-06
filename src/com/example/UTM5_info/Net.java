package com.example.UTM5_info;

import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by k on 06.08.14.
 */
public class Net {
    private final static String CAB_URL = "http://cab.inettel.ru/";
    private final static String LOG_TAG = "Net_log";

    private static Map<String, String> cookies = null;
    public static boolean connectFail;

    public static Document getMainPage(String login, String password) {
        connectFail = true;
        Document mainHtml;
        GetCookies task = new GetCookies();
        task.execute(login, password);
        try {
            mainHtml = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        if (connectFail) return null;
        if(mainHtml.select("table").isEmpty()) return null;
        return mainHtml;
    }

    public static Document getRequest(String url){
        Document html;
        GetRequest task = new GetRequest();
        task.execute(CAB_URL + url);
        try {
            html = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        return html;
    }

    public static Document postRequest(String url, Map<String, String> postData){
        Document html;
        url = CAB_URL + url;
        PostRequest task = new PostRequest();
        task.execute(url, postData);
        try {
            html = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        return html;
    }

    ///////////////////////////////////////////////////////////////////////////////

    private static class GetCookies extends AsyncTask<String, Void, Document>{
        @Override
        protected Document doInBackground (String... params) {
            Connection.Response response;
            Document html = null;
            try {
                response = Jsoup.connect(CAB_URL)
                        .data("bootstrap[username]", params[0])
                        .data("bootstrap[password]", params[1])
                        .method(Connection.Method.POST).execute();
                html = response.parse();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Login error");
                Net.cookies = null;
                return null;
            }
            Net.cookies = response.cookies();
            connectFail = false;
            return html;
        }
    }

    private static class GetRequest extends AsyncTask<String, Void, Document>{
        @Override
        protected Document doInBackground(String... url) {
            Document html;
            try {
                html = Jsoup.connect(url[0]).cookies(cookies).get();
            } catch (IOException e) {
                Log.d(LOG_TAG, "GET error");
                return null;
            }
            return html;
        }
    }

    private static class PostRequest extends AsyncTask<Object, Void, Document>{
        @Override
        protected Document doInBackground(Object... params) {
            Document html;
            String url = (String) params[0];
            Map<String, String> postData = (Map) params[1];
            try {
                html = Jsoup.connect(url)
                        .data(postData)
                        .cookies(cookies)
                        .post();
            } catch (IOException e) {
                Log.d(LOG_TAG, "POST error");;
                return null;
            }
            return html;
        }
    }
}
