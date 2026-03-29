package com.example.app.core;

/**
 * Abstract base class for microservices
 * All concrete services will extend this class
 */
public abstract class BaseService implements MicroService {
    
    protected String serviceName;
    protected int port;
    protected boolean running;
    
    public BaseService(String serviceName, int port) {
        this.serviceName = serviceName;
        this.port = port;
        this.running = false;
    }
    
    @Override
    public void start() {
        this.running = true;
        System.out.println("[" + serviceName + "] Service started on port " + port);
        onStart();
    }
    
    @Override
    public void stop() {
        this.running = false;
        System.out.println("[" + serviceName + "] Service stopped");
        onStop();
    }
    
    @Override
    public String getServiceName() {
        return serviceName;
    }
    
    @Override
    public int getPort() {
        return port;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Hook method called when service starts
     * Override this in subclasses for custom initialization
     */
    protected abstract void onStart();
    
    /**
     * Hook method called when service stops
     * Override this in subclasses for cleanup
     */
    protected abstract void onStop();
}
