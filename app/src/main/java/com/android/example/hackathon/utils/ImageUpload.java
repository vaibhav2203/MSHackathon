package com.android.example.hackathon.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

/*
 * Executes a new thread to upload image
 * */
public class ImageUpload implements Executor {

    @Override
    public void execute(Runnable command) {
        Thread thread = new Thread(command);
        thread.start();
    }

    public void executeQuery(String pathToImage, OnUploadCompleteListener onUploadCompleteListener) {
        ImageUpload obj = new ImageUpload();
        try {
            obj.execute(new NewThread(pathToImage, onUploadCompleteListener));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

/*
 * Creates a new thread
 * */
class NewThread implements Runnable {
    private String _pathToImage;
    private OnUploadCompleteListener _onUploadCompleteListener;

    public NewThread(String pathToImage, OnUploadCompleteListener onUploadCompleteListener) {
        _pathToImage = pathToImage;
        _onUploadCompleteListener = onUploadCompleteListener;
    }

    private String upload(String fileUploadPath) throws IOException {
        String filepath = fileUploadPath;
        File fileToUpload = new File(filepath);

        String endpoint = "/tags";

        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary = "Image Upload";

        URL urlObject = new URL("http://13.68.230.218:5000");
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream request = new DataOutputStream(connection.getOutputStream());

        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileToUpload.getName() + "\"" + crlf);
        request.writeBytes(crlf);


            InputStream inputStream = new FileInputStream(fileToUpload);
        int bytesRead;
        byte[] dataBuffer = new byte[1024];
        while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
            request.write(dataBuffer, 0, bytesRead);
        }

        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
        request.flush();
        request.close();

        InputStream responseStream = new BufferedInputStream(connection.getInputStream());

        BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

        String line = "";
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();
        // System.out.println(response);
        if (response.contains("\n")) {
            response = response.split("\n")[0];
        }
        responseStream.close();
        connection.disconnect();
        return response;
    }

    @Override
    public void run() {
        try {
            _onUploadCompleteListener.onUploadComplete(upload(_pathToImage));
        } catch (Exception e) {
            e.printStackTrace();
            _onUploadCompleteListener.errorOnUpload(e);
        }
    }

}
