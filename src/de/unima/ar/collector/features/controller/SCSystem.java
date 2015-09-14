package de.unima.ar.collector.features.controller;

public class SCSystem
{
    private static SCSystem SCSYSTEM;

    // private DataManager dataManager;
    private AttributeManager attributeManager;
    private WindowManager    windowManager;


    private SCSystem()
    {
        System.out.println("Starting System ...");

        // start thread - raw data
        // this.dataManager = new DataManager();
        // Thread dataManager = new Thread(this.dataManager);
        // dataManager.start();

        // start thread - parse data
        this.attributeManager = new AttributeManager();
        Thread attributeManager = new Thread(this.attributeManager);
        attributeManager.start();

        // start thread - create windows
        this.windowManager = new WindowManager();
        Thread windowManager = new Thread(this.windowManager);
        windowManager.start();

        // TODO do something with the existing windows... -

        System.out.println("> All threads were started!");
    }


    public static SCSystem getInstance()
    {
        if(SCSYSTEM == null) {
            DataCenter.getInstance();
            SCSYSTEM = new SCSystem();
        }

        return SCSYSTEM;
    }


    public void shutdown()
    {
        // this.dataManager.shutdown();
        this.attributeManager.shutdown();
        this.windowManager.shutdown();
    }


    public void clear()
    {
        SCSYSTEM = null;
    }
}