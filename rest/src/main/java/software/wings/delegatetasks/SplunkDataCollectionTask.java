package software.wings.delegatetasks;

import static software.wings.service.impl.splunk.SplunkDataCollectionTaskResult.Builder.aSplunkDataCollectionTaskResult;

import com.splunk.HttpService;
import com.splunk.Job;
import com.splunk.JobArgs;
import com.splunk.JobResultsArgs;
import com.splunk.ResultsReaderJson;
import com.splunk.SSLSecurityProtocol;
import com.splunk.Service;
import com.splunk.ServiceArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.beans.DelegateTask;
import software.wings.beans.SplunkConfig;
import software.wings.service.impl.splunk.SplunkDataCollectionInfo;
import software.wings.service.impl.splunk.SplunkDataCollectionTaskResult;
import software.wings.service.impl.splunk.SplunkDataCollectionTaskResult.SplunkDataCollectionTaskStatus;
import software.wings.service.impl.splunk.SplunkLogElement;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Inject;

/**
 * Created by rsingh on 5/18/17.
 */
public class SplunkDataCollectionTask extends AbstractDelegateRunnableTask<SplunkDataCollectionTaskResult> {
  private static final int DURATION_TO_ASK_MINUTES = 5;
  private static final SimpleDateFormat SPLUNK_START_DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final SimpleDateFormat SPLUNK_DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  private static final Logger logger = LoggerFactory.getLogger(SplunkDataCollectionTask.class);
  private final Object lockObject = new Object();
  private final AtomicBoolean completed = new AtomicBoolean(false);

  @Inject private SplunkMetricStoreService splunkMetricStoreService;

  public SplunkDataCollectionTask(String delegateId, DelegateTask delegateTask,
      Consumer<SplunkDataCollectionTaskResult> consumer, Supplier<Boolean> preExecute) {
    super(delegateId, delegateTask, consumer, preExecute);
  }

  @Override
  public SplunkDataCollectionTaskResult run(Object[] parameters) {
    final SplunkDataCollectionInfo dataCollectionInfo = (SplunkDataCollectionInfo) parameters[0];
    logger.info("log collection - dataCollectionInfo: {}" + dataCollectionInfo);

    final SplunkConfig splunkConfig = dataCollectionInfo.getSplunkConfig();
    final ServiceArgs loginArgs = new ServiceArgs();
    loginArgs.setUsername(splunkConfig.getUsername());
    loginArgs.setPassword(String.valueOf(splunkConfig.getPassword()));
    loginArgs.setHost(splunkConfig.getHost());
    loginArgs.setPort(splunkConfig.getPort());

    HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
    Service splunkService = Service.connect(loginArgs);
    final ScheduledExecutorService collectionService = scheduleMetricDataCollection(dataCollectionInfo, splunkService);
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    scheduledExecutorService.schedule(() -> {
      try {
        logger.info("log collection finished for " + dataCollectionInfo);
        collectionService.shutdown();
        collectionService.awaitTermination(1, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        collectionService.shutdown();
      }

      completed.set(true);
      synchronized (lockObject) {
        lockObject.notifyAll();
      }
    }, dataCollectionInfo.getCollectionTime() + 1, TimeUnit.MINUTES);
    logger.info("going to collect splunk data for " + dataCollectionInfo);

    synchronized (lockObject) {
      while (!completed.get()) {
        try {
          lockObject.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return aSplunkDataCollectionTaskResult().withStatus(SplunkDataCollectionTaskStatus.SUCCESS).build();
  }

  private ScheduledExecutorService scheduleMetricDataCollection(
      SplunkDataCollectionInfo dataCollectionInfo, Service splunkService) {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService.scheduleAtFixedRate(
        new SplunkDataCollector(dataCollectionInfo, splunkService, splunkMetricStoreService), 0, 1, TimeUnit.MINUTES);
    return scheduledExecutorService;
  }

  private static class SplunkDataCollector implements Runnable {
    private final SplunkDataCollectionInfo dataCollectionInfo;
    private final Service splunkService;
    private final SplunkMetricStoreService splunkMetricStoreService;

    private SplunkDataCollector(SplunkDataCollectionInfo dataCollectionInfo, Service splunkService,
        SplunkMetricStoreService splunkMetricStoreService) {
      this.dataCollectionInfo = dataCollectionInfo;
      this.splunkService = splunkService;
      this.splunkMetricStoreService = splunkMetricStoreService;
    }

    @Override
    public void run() {
      if (dataCollectionInfo.getCollectionTime() <= 0) {
        return;
      }

      try {
        for (String query : dataCollectionInfo.getQueries()) {
          final String searchQuery = "search " + query + " | bin _time span=1m | cluster showcount=t labelonly=t"
              + "| table _time, _raw,cluster_label, host | "
              + "stats latest(_raw) count by _time,cluster_label,host";
          JobArgs jobargs = new JobArgs();
          jobargs.setExecutionMode(JobArgs.ExecutionMode.BLOCKING);

          final Date startTime = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
          jobargs.setEarliestTime(SPLUNK_START_DATE_FORMATER.format(startTime));
          jobargs.setLatestTime("now");

          // A blocking search returns the job when the search is done
          logger.debug("triggering query " + searchQuery);
          Job job = splunkService.getJobs().create(searchQuery, jobargs);
          logger.debug("splunk query done. Num of events: " + job.getEventCount());

          JobResultsArgs resultsArgs = new JobResultsArgs();
          resultsArgs.setOutputMode(JobResultsArgs.OutputMode.JSON);

          InputStream results = job.getResults(resultsArgs);
          ResultsReaderJson resultsReader = new ResultsReaderJson(results);
          HashMap<String, String> event;
          final List<SplunkLogElement> logElements = new ArrayList<>();
          while ((event = resultsReader.getNextEvent()) != null) {
            final SplunkLogElement splunkLogElement = new SplunkLogElement();
            splunkLogElement.setClusterLabel(event.get("cluster_label"));
            splunkLogElement.setHost(event.get("host"));
            splunkLogElement.setCount(Integer.parseInt(event.get("count")));
            splunkLogElement.setLogMessage(event.get("latest(_raw)"));
            splunkLogElement.setTimeStamp(SPLUNK_DATE_FORMATER.parse(event.get("_time")).getTime());
            logElements.add(splunkLogElement);
          }
          resultsReader.close();
          splunkMetricStoreService.save(
              dataCollectionInfo.getAccountId(), dataCollectionInfo.getApplicationId(), logElements);
        }
        dataCollectionInfo.setCollectionTime(dataCollectionInfo.getCollectionTime() - 1);
      } catch (Exception e) {
        logger.error("error fetching splunk logs", e);
      }
    }
  }

  public static void main(String[] args) throws ParseException {
    String dateString = "2017-06-19T18:02:00.000-07:00";
    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    Date date = sdf.parse(dateString);
    System.out.println(date);
    System.out.println(sdf.format(new Date()));
  }
}
