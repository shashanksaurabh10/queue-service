package com.example;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.HttpURLConnection;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class KafkaQueueServiceTest {

    private HttpURLConnection mockConnection;
    private QueueService queueService = new KakfkaQueueService();


    @Before
    public void setUp() throws IOException {
        // Arrange
        queueService = Mockito.spy(new KakfkaQueueService());
        mockConnection = mock(HttpURLConnection.class);

        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("response body".getBytes()));
        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        doAnswer(invocation -> {
            String url = invocation.getArgument(0);
            String method = invocation.getArgument(1);

            when(mockConnection.getRequestMethod()).thenReturn(method);
            when(mockConnection.getRequestProperty("Authorization")).thenReturn("Basic " + "some properties");
            when(mockConnection.getDoInput()).thenReturn(true);
            when(mockConnection.getOutputStream()).thenReturn(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });
            return mockConnection;
        }).when(queueService).push(anyString(), anyString());

        doAnswer(invocation -> {
            String url = invocation.getArgument(0);

            when(mockConnection.getRequestMethod()).thenReturn("GET");
            when(mockConnection.getRequestProperty("Authorization")).thenReturn("Basic " + "something");
            when(mockConnection.getDoInput()).thenReturn(true);

            return new Message("response body" , "receiptId");
        }).when(queueService).pull(anyString());
    }
    @Test(expected = RuntimeException.class)
    public void pullMessgeException(){

        String queueUrl = null;

        Message message = queueService.pull(queueUrl);
    }

    @Test
    public void pullShouldReturnValidMessage() throws IOException {

        String queueUrl = "queueurl/test";

        Message message = queueService.pull(queueUrl);

        assertNotNull("Message should not be null", message);
        assertEquals("Message body should match", "response body", message.getBody());
        assertNotNull("Receipt Id should not be null" , message.getReceiptId());
    }

    @Test()
    public void pushMessage() {

        String queueUrl = "queuUrl";
        String messageBody = "message content";

        queueService.push(queueUrl, messageBody);


    }
}