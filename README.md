#Java JMX Statistics Suite

##What is it?
A discreet reusable statistics counter providing access through Jconsole (JMX).

###Features

 - incremental statistics counting
 - rolling average statistics
 - available through JMX and Jconsole
 - configurable (en/disable, en/disable logging, en/disable rolling) through Jconsole
 - statistics logging through the applications logger
 - standard access through StatisticsService interface
 - annotation based access
 - conditional stats based on the method parameters and/or result
 - dynamic naming based on the  method parameters and/or result
 - easy setup through Spring or AspectJ


##Usage

### Standard Access

For standard access you simply need to create an instance of JMXStatisticsService and use it as you need. Keep in mind that there is a Thread per instance to look after the cleanup so probably best to have one shared instance.


    // Instantiate the JMXStatisticsService
	StatisticsService statsService = new JMXStatisticsService();
     
    // This step is optional.  It is used if you want to specify a different objectName than the default one.
    // This call will create a new MBean and register the statistics with the ObjectName:
    // my.domain.stats:type=MyService
    service.createStatsMBean("my.domain",  MyService.class);
     
    // Creates a new Statistic called Test1 and associates it with MyService.class.
    // If createStatsMBean was not called as above, then this call will create the MBean
    // and register the statistics with the default ObjectName:
    // {MyService.package}.stats:type=MyService
    service.initialiseStats("Test1", MyService.class);
     
    // increment the Test1 statistic by one
    statsService.incrementStats("Test1", MyService.class);
     
    // Same as above though it will also log to the applications logger
    statsService.incrementStatsAndLog("Test2", MyService.class, true);
     
    // Same as Test1, but will also keep and provide a rolling avg over a 1 second window
    statsService.incrementStats("Test3", MyService.class, ROLLING_AVG_WINDOW.SECOND); 
     
    // Same as Test2, but will also keep and provide a rolling avg over a 1 second window
    statsService.incrementStatsAndLog("Test4", MyService.class, ROLLING_AVG_WINDOW.SECOND, true);



### Annotations

For annotations to work you'll need to either use Spring or AspectJ to set things up. I have described below how to configure things using AspectJ's [load-time](http://www.eclipse.org/aspectj/doc/next/devguide/ltw.html) weaving. 
You could of course use [compile-time](http://www.eclipse.org/aspectj/doc/next/devguide/ajc-ref.html) weaving which has the added benefit of faster load times (depending on the size of your codebase), but with more complexity for setting up in my opinion. 
If you are using [Maven](http://mojo.codehaus.org/aspectj-maven-plugin/compile-mojo.html), then things become a bit easier.


    // If using Spring and you want to expose other methods or attributes to JMX
    //@ManagedResource("test.service:type=MyAnnotatedService")
     
    // Or if using AspectJ only
    @StatsManagedResource("test.service.stats:type=MyAnnotatedService")
    public class MyAnnotatedService {
     
     // Creates a new Statisitc called AlwaysIncrement and associates it with MyAnnotatedService.class.
     // This will register the statistics with the ObjectName: test.service.stats:type=MyAnnotatedService
     // This statistic will increment on every execution of this method
     @IncrementStat("AlwaysIncrement")
     public void AlwaysIncrement(){
      // .. do something
     }
     
     // Same as AlwaysIncrement, though it will only increment if the method returns true
     @IncrementStat(value="IncrementWhenReturnTrue", condition="#result == true")
     public boolean IncrementWhenReturnTrue(){
      // .. do something
      return true;
     }
     
     // Similar to AlwaysIncrement, however for every value of the parameter 'name' will result in an individual 
     // statistic named 'Dynamic #name' where #name is the value of the parameter at runtime
     @IncrementStat("Dynamic #name")
     public boolean DynamicName(String name){
      // .. do something
      return true;
     }
     // Same as IncrementWhenReturnTrue, though it will only increment if the provided name equals 'Harry' and the method returns true 
     @IncrementStat(value="NameEqualsHarryAndReturnTrue", condition="#name == 'Harry' && #result == true")
     public boolean NameEqualsHarryAndReturnTrue(String name){
      // .. do something
      return true;
     }
     
     // Same as NameEqualsHarryAndReturnTrue, but it will also log the statistic if logging is enabled for debug
     @IncrementStat(value="NameEqualsHarryAndReturnTrue", condition="#name == 'Harry' && #result == true", logLevel=LOG_LEVEL.DEBUG)
     public boolean NameEqualsHarryAndReturnTrueWithLogging(String name){
      // .. do something
      return true;
     }
     
     // Will only increment if the given ServiceException is thrown
     @IncrementStat(value="ThrowsServiceException", throwing=ServiceException.class)
     public void ThrowsServiceException(String name) throws ServiceException {
      try {
       // .. do something
      } catch (ServiceException e) {
       // .. do something
       throw e;
      }
     }
      
    // Multiple annotations on one method
     @IncrementStats({
      @IncrementStat(value="ThrowsServiceException", throwing=ServiceException.class),
      @IncrementStat(value="NameEqualsHarryAndReturnTrue", condition="#name == 'Harry' && #result == true"),  
      @IncrementStat(value="IncrementWhenReturnTrue", condition="#result == true")})
     public boolean MultiStatistic(String name) throws ServiceException {
      try {
       // .. do something
       return true;
      } catch (ServiceException e) {
       // .. do something
       throw e;
      }
     }
      
     // Same as AlwaysIncrement, though it will also provide an average count since the 
     // start of the application of a period of 1 second
     @IncrementStat(value="AverageOverSecond", rollingAvgWindow = ROLLING_AVG_WINDOW.SECOND)
     public void AverageOverSecond(){
      // .. do something
     }
    }


### Setup with AspectJ
Ensure you have Spring aop, expressions and aspectjrt.jar and their dependencies in your classpath. You also need to add the aspectjweaver.jar as a javaagent of the JVM:
-javaagent:aspectjweaver.jar

Then just create a file META-INF/aop.xml and ensure it is in the classpath.

    <aspectj>
     <aspects>
      <!-- The Aspect -->
      <aspect name="org.gw.stats.aop.AnnotationDrivenJMXStatisticsService"/>
     </aspects>
     <weaver options="-verbose -showWeaveInfo">
      <!-- Weave types that are within the my.package.* packages. Add more if needed. -->
      <include within="my.package.*" />
      <!-- <include within="org.other.*" /> -->
       
      <!-- Include the Aspect class here as well -->
      <include within="org.gw.stats.aop.AnnotationDrivenJMXStatisticsService"/>
     </weaver>
    </aspectj>

Once you have AspectJ configured, its just a matter of getting a reference to the AnnotationDrivenJMXStatisticsService that AspectJ instantiated and set the base packages. 

    public static void main(String[] args) throws InterruptedException {
      AnnotationDrivenJMXStatisticsService statsService = Aspects.aspectOf(AnnotationDrivenJMXStatisticsService.class); 
      statsService.setBasePackage("org.other", "com.example");
      statsService.init();
      // This will check com.example and org.other packages for classes annotated with @StatsManagedResource
     
      // ... do something
     
      // If you also want to increment the stats the standard way you can
      statsService.incrementStats("Test Stat", MyService.class, ROLLING_AVG_WINDOW.SECOND); 
    }


### Setup with Spring
One limitation of using Spring over AspectJ is that you will only be able to collect stats on Spring Beans. And then only if the method being called is from outside it's own instance.

Another small difference when using Spring, if you already have set the Spring Bean up as a JMX managed bean either through the @ManagedBean annotation or in XML config, there is no need to add @StatsManagedResource annotation to the type declaration. The domain of the ObjectName is appended with .stats so as to distinguish it from the Spring managed bean.

Ensure you have the Spring core, aop and expressions and all their dependencies in your classpath. You also need to add the spring-instrument as a javaagent of the JVM:
-javaagent:spring-instrument-3.2.2.RELEASE.jar

    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:aop="http://www.springframework.org/schema/aop"
     xmlns:context="http://www.springframework.org/schema/context"
     xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
      http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.2.xsd
      http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.2.xsd">
        <!-- JMX Setup -->
        <context:mbean-export default-domain="my.package" />
         
        <!-- SETUP Spring context load time weaver -->
        <context:load-time-weaver />
         
        <aop:aspectj-autoproxy proxy-target-class="true" />
         
        <context:annotation-config />
         
        <context:component-scan base-package="org.gw.stats.aop" />
        <context:component-scan base-package="org.gw.my.package" />
    </beans>

### Configuration

When using annotations for statistics, there is a bit of a performance hit on startup to trawl the codebase looking for the @StatsManagedResource annotation, so *statistics.base.packages* system property is provided so that you can specify the base packages for scanning. Similar to Spring's component-scan. 

Similarly, if you have a ton of MBeans in the MBean server you can provide a *statistics.mbean.domain.prefix.filters* System property to speed things up when scanning the MBean server. 

The statistics suite can also be customised somewhat. If you want to change the MBean domain addition you can provide the *statistics.mbean.domain.addon* System property. The *statistics.log.format* System property updates the log message when logging your statistic.


| System property | Description |  default |
| :--------------- | :------------- | :---------- |
|statistics.base.packages | This is the list of base packages (comma separated) that the suite will look for @StatsManagedResource annotation, and consequently the @IncrementStats and @IncrementStat method annotations.	 | |
| statistics.mbean.domain.prefix.filters | The MBean domain filter list (comma separated) for looking up MBeans on the platform MBeanServer that could have @IncrementStats and @IncrementStat method annotations. | |
| statistics.mbean.domain.addon | The MBean domain addon to differentiate the MBean added by the application with the one added for statistics. | .stats |
| statistics.log.format | The log format when logging a statistic. There are 2 parameters available, the first is the stat name and the second is the count. | Statistic logging: %s = %s |
 

### JMX and Jconsole
Given the example above, the mbeans tab of jconsole provides you with the statistics as attributes of the MyAnnotationService MBean registered on the MBean server under domain 'test.service.stats'.

The MBean will also have a bunch of operations which provide you with further control on the statistics once the application has started. You can enable/disable one or all statistics, enable/disable the rolling average for a statistic, enable/disable logging, reset one or all statistics, set the rolling window for one or all statistics to name a few.

**Note:** The rollingAvgWindow attribute is only available if you provide the rollingAvgWindow in the annotation declaration. 

##Design 
The statistics service comes in two flavours; standard and annotation based.

The standard base uses the JMXStatisticsService and does not require any additional setup. However it does mean that you will have to instantiate the service and include it as a reference to any class that wants to use it.

The annotation base uses the `AnnotationDrivenJMXStatisticsService` which adds AOP support for the `@IncrementStats` and `@IncrementStat` method annotations, as well as the `@StatsManagedResource` type annotation. This is made possible because `AnnotationDrivenJMXStatisticsService` can also be declared as an AspectJ Aspect. It implements 2 methods, one for each method annotation which can be either compile-time or load-time weaved by AspectJ through the application code base. In a nutshell, those two methods can intercept all method calls on methods annotated with `@IncrementStats` and `@IncrementStat` annotations. At which point, depending on the config of the annotation will increment the statistic defined by that annotation.

The `org.gw.stats.jmx` package provides the model objects for all JMX attributes and operations for the statistics.