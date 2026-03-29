package com.example.app.client;

/**
 * ServiceCommunicationException - Thrown when there's an error communicating with a microservice
 */
public class ServiceCommunicationException extends RuntimeException {
    
    public ServiceCommunicationException(String message) {
        super(message);
    }
    
    public ServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
