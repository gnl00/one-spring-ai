package one.demo;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class OkMain {
    public static void main(String[] args) {
        OkHttpClient.Builder  builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.build();
        try {
            Request.Builder  requestBuilder = new Request.Builder();
            RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json"));
            Request request = requestBuilder.url("").post(requestBody).addHeader("Authorization", "").build();
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
