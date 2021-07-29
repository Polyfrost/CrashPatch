package vfyjxf.bettercrashes.utils;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CrashReportUpload {


    public static String uploadToUbuntuPastebin(String url, String crashReport) throws IOException {
        URL pasteUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) pasteUrl.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setReadTimeout(1000000);
        connection.setConnectTimeout(500000);
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        String params = "poster=CrashReport&syntax=text&content=" + URLEncoder.encode(crashReport, "UTF-8");
        out.write(params);
        out.flush();
        String resultUrl = null;
        resultUrl = connection.getHeaderField("Location");
        if (resultUrl == null) {
            resultUrl = connection.getHeaderField("location");
        }
        return url + resultUrl;
    }

}
