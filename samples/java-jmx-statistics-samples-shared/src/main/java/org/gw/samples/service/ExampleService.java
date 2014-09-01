package org.gw.samples.service;


import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public interface ExampleService {

    @ManagedOperation
    void AlwaysIncrement();

    @ManagedOperation
    boolean IncrementOnReturnTrue();

    @ManagedOperation
    @ManagedOperationParameter(name = "Name", description = "Name")
    boolean DynamicName(String name);

    @ManagedOperation
    @ManagedOperationParameter(name = "Name", description = "Name")
    boolean NameEqualsHarry(String name);

    @ManagedOperation
    @ManagedOperationParameter(name = "Name", description = "Name")
    boolean NameEqualsHarryIfReturnTrueAlsoLog(String name);

    @ManagedOperation
    @ManagedOperationParameter(name = "Name", description = "Name")
    void IncrementOnServiceException(String name)
            throws ServiceException;

    @ManagedOperation
    @ManagedOperationParameter(name = "Name", description = "Name")
    boolean MultipleStats(String name) throws ServiceException;

    @ManagedOperation
    void AverageOverSecond();

}