/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.core.bus;

import io.onemfive.core.*;
import io.onemfive.core.admin.AdminService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.keyring.KeyRingService;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.orchestration.OrchestrationService;
import io.onemfive.monetary.bsq.BisqService;
import io.onemfive.monetary.btc.BitcoinService;
import io.onemfive.monetary.kmd.KomodoService;
import io.onemfive.monetary.xmr.MoneroService;
import io.onemfive.util.AppThread;
import io.onemfive.data.Envelope;
import io.onemfive.util.Config;
import io.onemfive.util.DLC;
import io.onemfive.did.DIDService;
import io.onemfive.network.NetworkService;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Encompasses all functionality needed to support messaging between
 * all internal services and their life cycles.
 *
 * Provides a Staged Event-Driven Architecture (SEDA) by providing a
 * channel to/from all Services.
 *
 * All bus threads come from one pool to help manage resource usage.
 *
 * TODO: Handles high priority commands to manage the services synchronously.
 * TODO: Add configurations
 * TODO: Enable extending thread pool so services can pull from the same pool thus ensuring better performance management
 *
 * @author objectorange
 */
public final class ServiceBus implements MessageProducer, LifeCycle, ServiceRegistrar, ServiceStatusListener {

    private static final Logger LOG = Logger.getLogger(ServiceBus.class.getName());

    public enum Status {Starting, Running, Stopping, Stopped}

    private Status status = Status.Stopped;

    private Properties properties;

    private volatile WorkerThreadPool pool;
    private MessageChannel channel;

    private ClientAppManager clientAppManager;
    private Map<String, BaseService> registeredServices;
    private Map<String, BaseService> runningServices;

    private List<BusStatusListener> busStatusListeners = new ArrayList<>();

    // TODO: Set maxThreads by end-user max processing allocation (Prana limitations)
    private int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    // TODO: Set maxMessagesCached by end-user max memory allocation (Prana limitations)
    private int maxMessagesCached = 10 * maxThreads;

    private final AtomicBoolean spin = new AtomicBoolean(true);

    public ServiceBus(Properties properties, ClientAppManager clientAppManager) {
        this.properties = properties;
        this.clientAppManager = clientAppManager;
        LOG.finer("Instantiated with maxThreads="+maxThreads+" and maxMessagesCached="+maxMessagesCached);
    }

    @Override
    public boolean send(Envelope e) {
        LOG.finest("Received envelope. Sending to channel...");
        if(pool != null && pool.getStatus() == WorkerThreadPool.Status.Running) {
            return channel.send(e);
        } else {
            String errMsg = "Unable to send to channel: pool.status="+pool.getStatus().toString();
            DLC.addErrorMessage(errMsg, e);
            LOG.warning(errMsg);
            return false;
        }
    }

    public void registerBusStatusListener (BusStatusListener busStatusListener) {
        busStatusListeners.add(busStatusListener);
    }

    public void unregisterBusStatusListener(BusStatusListener busStatusListener) {
        busStatusListeners.remove(busStatusListener);
    }

    public void registerService(Class serviceClass, Properties p, List<ServiceStatusObserver> observers) throws ServiceNotAccessibleException, ServiceNotSupportedException, ServiceRegisteredException {
        LOG.info("Registering service class: "+serviceClass.getName());
        if(registeredServices.containsKey(serviceClass.getName())) {
            throw new ServiceRegisteredException();
        }
        if(p != null && p.size() > 0)
            properties.putAll(p);
        final String serviceName = serviceClass.getName();
        try {
            final BaseService service = (BaseService)serviceClass.newInstance();
            service.setProducer(this);
            // register service
            registeredServices.put(serviceClass.getName(), service);
            service.registerServiceStatusListener(this);
            service.setRegistered(true);
            if(observers != null) {
                LOG.info("Registering ServiceStatusObservers with service: "+service.getClass().getName());
                registerServiceStatusObservers(serviceClass, observers);
            }
            LOG.info("Service registered successfully: "+serviceName);
            // init registered service
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    if(service.start(properties)) {
                        runningServices.put(serviceName, service);
                        LOG.info("Service registered successfully as running: "+serviceName);
                    } else {
                        LOG.warning("Registered service failed to start: "+serviceName);
                    }
                }
            }, serviceName+"-StartupThread").start();
        } catch (InstantiationException e) {
            throw new ServiceNotSupportedException(e);
        } catch (IllegalAccessException e) {
            throw new ServiceNotAccessibleException(e);
        }
    }

    public void unregisterService(Class serviceClass) {
        if(runningServices.containsKey(serviceClass.getName())) {
            final String serviceName = serviceClass.getName();
            final BaseService service = runningServices.get(serviceName);
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    if(service.shutdown()) {
                        runningServices.remove(serviceName);
                        registeredServices.remove(serviceName);
                        service.setRegistered(false);
                        LOG.finer("Service unregistered successfully: "+serviceName);
                    }
                }
            }, serviceName+"-ShutdownThread").start();
        }
    }

    public void registerServiceStatusObservers(Class serviceClass, List<ServiceStatusObserver> observers) {
        if(registeredServices.containsKey(serviceClass.getName())) {
            BaseService service = registeredServices.get(serviceClass.getName());
            LOG.info("Registering ServiceStatusObservers with service: "+service.getClass().getName());
            service.registerServiceStatusObservers(observers);
        }
    }

    public void unregisterServiceStatusObservers(Class serviceClass, ServiceStatusObserver observer) {
        if(registeredServices.containsKey(serviceClass.getName())) {
            BaseService service = registeredServices.get(serviceClass.getName());
            LOG.info("Unregistering ServiceStatusObserver with service: "+service.getClass().getName());
            service.unregisterServiceStatusObserver(observer);
        }
    }

    public List<ServiceReport> listServices(){
        List<ServiceReport> serviceReports = new ArrayList<>(registeredServices.size());
        ServiceReport r;
        for(BaseService s : registeredServices.values()) {
            r = new ServiceReport();
            r.registered = true;
            r.running = runningServices.containsKey(s.getClass().getName());
            r.serviceClassName = s.getClass().getName();
            r.serviceStatus = s.getServiceStatus();
            serviceReports.add(r);
        }
        return serviceReports;
    }

    private void updateStatus(Status status) {
        this.status = status;
        switch(status) {
            case Starting: {
                LOG.info("1M5 Service Bus is Starting");
                break;
            }
            case Running: {
                LOG.info("1M5 Service Bus is Running");
                break;
            }
            case Stopping: {
                LOG.info("1M5 Service Bus is Stopping");
                break;
            }
            case Stopped: {
                LOG.info("1M5 Service Bus has Stopped");
                break;
            }
        }
        LOG.info("Updating Bus Status Listeners; size="+busStatusListeners.size());
        for(BusStatusListener l : busStatusListeners) {
            l.busStatusChanged(status);
        }
    }

    @Override
    public void serviceStatusChanged(String serviceFullName, ServiceStatus serviceStatus) {
        LOG.info("Service ("+serviceFullName+") reporting new status("+serviceStatus.name()+") to Bus.");
        switch(serviceStatus) {
            case UNSTABLE: {
                // Service is Unstable - restart
                BaseService service = registeredServices.get(serviceFullName);
                if(service != null) {
                    LOG.warning("Service ("+serviceFullName+") reporting UNSTABLE; restarting...");
                    service.restart();
                }
                break;
            }
            case RUNNING: {
                if(allServicesWithStatus(ServiceStatus.RUNNING)) {
                    LOG.info("All Services are RUNNING therefore Bus updating status to RUNNING.");
                    updateStatus(Status.Running);
                }
                break;
            }
            case SHUTDOWN: {
                if(allServicesWithStatus(ServiceStatus.SHUTDOWN)) {
                    LOG.info("All Services are SHUTDOWN therefore Bus updating status to STOPPED.");
                    updateStatus(Status.Stopped);
                }
                break;
            }
            case GRACEFULLY_SHUTDOWN: {
                if(allServicesWithStatus(ServiceStatus.GRACEFULLY_SHUTDOWN)) {
                    LOG.info("All Services are GRACEFULLY_SHUTDOWN therefore Bus updating status to STOPPED.");
                    updateStatus(Status.Stopped);
                }
                break;
            }
        }
    }

    private Boolean allServicesWithStatus(ServiceStatus serviceStatus) {
        Collection<BaseService> services = registeredServices.values();
        for(BaseService s : services) {
            if(s.getServiceStatus() != serviceStatus){
                return false;
            }
        }
        return true;
    }

    /**
     * Starts up Service Bus registering internal services, starting all services registered, and starting message channel
     * and worker thread pool.
     *
     * TODO: Provide a method for externalizing service registration record for startup.
     *
     * @param properties
     * @return
     */
    @Override
    public boolean start(Properties properties) {
        updateStatus(Status.Starting);
        if(properties != null) {
            if(this.properties == null)
                this.properties = properties;
            else
                this.properties.putAll(properties);
        }

        // TODO: should we init the pool before the channel?
        channel = new MessageChannel(maxMessagesCached);
        channel.start(properties);

        registeredServices = new HashMap<>(15);
        runningServices = new HashMap<>(15);

        final Properties props = this.properties;
        // Register Core Services - Place slowest to RUNNING services first
        InfoVaultService infoVaultService = new InfoVaultService(this, this);
        registeredServices.put(InfoVaultService.class.getName(), infoVaultService);
        // Start InfoVaultService first to ensure InfoVaultDB gets initialized before Services begin using it.
        infoVaultService.start(props);
        runningServices.put(InfoVaultService.class.getName(),infoVaultService);

        OrchestrationService orchestrationService = new OrchestrationService(this, this);
        registeredServices.put(OrchestrationService.class.getName(), orchestrationService);

        KeyRingService keyRingService = new KeyRingService(this, this);
        registeredServices.put(KeyRingService.class.getName(), keyRingService);

        NotificationService notificationService = new NotificationService(this, this);
        registeredServices.put(NotificationService.class.getName(), notificationService);

        DIDService didService = new DIDService(this, this);
        registeredServices.put(DIDService.class.getName(), didService);

        NetworkService networkService = new NetworkService(this, this);
        registeredServices.put(NetworkService.class.getName(), networkService);

        BisqService bisqService = new BisqService(this, this);
        registeredServices.put(BisqService.class.getName(), bisqService);

        BitcoinService bitcoinService = new BitcoinService(this, this);
        registeredServices.put(BitcoinService.class.getName(), bitcoinService);

        KomodoService komodoService = new KomodoService(this, this);
        registeredServices.put(KomodoService.class.getName(), komodoService);

        MoneroService moneroService = new MoneroService(this, this);
        registeredServices.put(MoneroService.class.getName(), moneroService);

        AdminService adminService = new AdminService(this, this);
        registeredServices.put(AdminService.class.getName(), adminService);

        // Start Registered Services
        for(final String serviceName : registeredServices.keySet()) {
            if(!serviceName.equals(InfoVaultService.class.getName())) {
                // InfoVaultService already started above
                new AppThread(new Runnable() {
                    @Override
                    public void run() {
                        BaseService service = registeredServices.get(serviceName);
                        if (service.start(props)) {
                            runningServices.put(serviceName, service);
                        }
                    }
                }, serviceName + "-StartupThread").start();
            }
        }

        pool = new WorkerThreadPool(clientAppManager, runningServices, channel, maxThreads, maxThreads, properties);
        pool.start();

        return true;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean unpause() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    /**
     * Shutdown the Service Bus
     *
     * TODO: Run in separate AppThread
     *
     * @return
     */
    @Override
    public boolean shutdown() {
        updateStatus(Status.Stopping);
        spin.set(false);
        pool.shutdown();
        channel.shutdown(); // TODO: Should we teardown channel before pool?
        for(final String serviceName : runningServices.keySet()) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    BaseService service = runningServices.get(serviceName);
                    if(service.shutdown()) {
                        runningServices.remove(serviceName);
                    }
                }
            }, serviceName+"-ShutdownThread").start();
        }
        return true;
    }

    /**
     * Ensure teardown is graceful by waiting until all Services indicate graceful teardown complete or timeout
     *
     * TODO: Implement
     *
     * @return
     */
    @Override
    public boolean gracefulShutdown() {
        updateStatus(Status.Stopping);
        spin.set(false);
        pool.shutdown();
        channel.shutdown(); // TODO: Should we teardown channel before pool?
        for(final String serviceName : runningServices.keySet()) {
            new AppThread(new Runnable() {
                @Override
                public void run() {
                    BaseService service = runningServices.get(serviceName);
                    if(service.gracefulShutdown()) {
                        runningServices.remove(serviceName);
                    }
                }
            }, serviceName+"-GracefulShutdownThread").start();
        }
        return true;
    }

    public Status getStatus() {
        return status;
    }
}
