package com.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class KakfkaQueueService implements QueueService {
    private static final Properties credentials;

    static {
        credentials = getCredentials();
    }

    private static Properties getCredentials() {
        Properties props = new Properties();
        String credFileName = "kafkaconfig.credentials";
        try (InputStream inStream = KakfkaQueueService.class.getClassLoader().getResourceAsStream(credFileName)) {
            props.load(inStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading credentials file", e);
        }
        return props;
    }

    private HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Basic " + credentials.getProperty("basic.auth.user.info"));
        connection.setDoInput(true);
        return connection;
    }

    private void writeRequestBody(HttpURLConnection connection, String messageBody) throws IOException {
        connection.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(messageBody);
            wr.flush();
        }
    }

    private String readResponseBody(HttpURLConnection connection) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    @Override
    public void push(String queueUrl, String messageBody) {
        try {
            HttpURLConnection connection = openConnection(queueUrl, "POST");
            writeRequestBody(connection, messageBody);
            String response = readResponseBody(connection);
            System.out.println(response);
        } catch (IOException e) {
            throw new RuntimeException("Error pushing message", e);
        }
    }

    @Override
    public Message pull(String queueUrl) {
        try {
            HttpURLConnection connection = openConnection(queueUrl, "GET");
            String response = readResponseBody(connection);
            return new Message(response);
        } catch (IOException e) {
            throw new RuntimeException("Error pulling message", e);
        }
    }

    @Override
    public void delete(String queueUrl, String receiptId) {
        // Deleting in Kafka server is not possible through upstash
    }
}