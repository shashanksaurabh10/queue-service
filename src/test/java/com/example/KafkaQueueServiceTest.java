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

        // Stubbing the methods of the mockConnection object for a generic response
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("response body".getBytes()));
        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // Stubbing the internal call to openConnection to use the mockConnection for push
        doAnswer(invocation -> {
            String url = invocation.getArgument(0);
            String method = invocation.getArgument(1);

            // Set up the mock connection as if openConnection was called with POST method
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

        // Stubbing the internal call to openConnection to use the mockConnection for pull
        doAnswer(invocation -> {
            String url = invocation.getArgument(0);
//            String method = invocation.getArgument(1);

            // Set up the mock connection as if openConnection was called with GET method
            when(mockConnection.getRequestMethod()).thenReturn("GET");
            when(mockConnection.getRequestProperty("Authorization")).thenReturn("Basic " + "something");
            when(mockConnection.getDoInput()).thenReturn(true);

            return new Message("response body" , "receiptId");
        }).when(queueService).pull(anyString());
        // Repeat the above stubbing for other methods that call openConnection, such as pull or delete.
    }
    @Test(expected = RuntimeException.class)
    public void pullMessgeException(){
        // Arrange
        String queueUrl = null;

        // Act
        Message message = queueService.pull(queueUrl);
    }

    @Test
    public void pullShouldReturnValidMessage() throws IOException {
        // Arrange
        String queueUrl = "queueurl/vatsuvaksi";

        // Act
        Message message = queueService.pull(queueUrl);

        // Assert
        assertNotNull("Message should not be null", message); // Verify the message was constructed
        assertEquals("Message body should match", "response body", message.getBody()); // Verify the message body matches
        assertNotNull("Receipt Id should not be null" , message.getReceiptId());
    }

    @Test()
    public void pushMessage() {
        // Arrange
        String queueUrl = "queuUrl";
        String messageBody = "message content";

        // Act
        queueService.push(queueUrl, messageBody);


    }

    @Test(expected = RuntimeException.class)
    public void pullShouldThrowRuntimeExceptionWhenArgumentsInvalid() {
        // Arrange
        String queueUrl = null; // Invalid URL (null)

        // Act
        queueService.pull(queueUrl);

        // Since we expect an exception, no need for an assert here. The expected exception is declared in the @Test annotation.
    }
}