#Samples

I have provided a couple of samples in this folder to get you started. Once you have downloaded the source, you should be able to run them using gradle. 

####Prerequisites

- Java 6 JDK
- Gradle 2.0+

####Running the samples

If using an IDE and it supports Gradle. Once you have imported the projects, open the Gradle view select the target 'run' on the desired sample.

... or on the command line ...

- cd {java-jmx-statistics-parent}/samples/{sample}
- gradle run
- Open up JConsole and have a look at the MBeans tab for the statistics.

##Standard Sample

The standard sample shows how to use the java-jmx-statistics suite without any bells or whistles. Simply include the jar file in your classpath, instantiate a JMXStatisticsService and away you go.


    // Instantiate the JMXStatisticsService
	StatisticsService statsService = new JMXStatisticsService();
     
    // This step is optional.  It is used if you want to specify a different objectName than the default one.
    // This call will create a new MBean and register the statistics with the ObjectName:
    // my.domain.stats:type=MyService
    service.createStatsMBean("my.domain",  MyService.class);
    

##Spring Sample

The Spring sample showcases the annotations mixed in with Spring @ManagedResource(s).



    // If using Spring and you want to expose other methods or attributes to JMX
    @ManagedResource("test.service:type=MyAnnotatedService")
    public class MyAnnotatedService {
     
	     // Creates a new Statisitc called AlwaysIncrement and associates it with MyAnnotatedService.class.
	     // This will register the statistics with the ObjectName: test.service.stats:type=MyAnnotatedService
	     // This statistic will increment on every execution of this method
	     @IncrementStat("AlwaysIncrement")
	     @ManagedOperation
	     public void AlwaysIncrement(){
	      // .. do something
	     }
     }


##AspectJ Sample

The AspectJ sample showcases the ability to use @StatsManagedResource type annotation, as well as being able to track statistics on non-Spring managed beans.

	@StatsManagedResource("test.stats:type=MyObject")
    public class MyObject {

	    @IncrementStat(value = "Dynamic #name")
	    public void DynamicName(String name) throws Exception {
	        // .. do something
	        if (name == null) {
	            throw new Exception();
	        }
	    }
     }

