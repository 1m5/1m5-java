package io.onemfive.core;

import io.onemfive.core.bus.ServiceNotAccessibleException;
import io.onemfive.core.bus.ServiceNotSupportedException;
import io.onemfive.core.bus.ServiceRegisteredException;

import java.util.List;
import java.util.Properties;

/**
 * The responsibility to register and unregister a service along with its observers.
 *
 * @author objectorange
 */
public interface ServiceRegistrar {

    void registerService(Class serviceClass, Properties properties, List<ServiceStatusObserver> observers) throws ServiceNotAccessibleException, ServiceNotSupportedException, ServiceRegisteredException;

    void unregisterService(Class serviceClass);
}
