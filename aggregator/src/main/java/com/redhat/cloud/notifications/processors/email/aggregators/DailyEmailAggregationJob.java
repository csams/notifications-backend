package com.redhat.cloud.notifications.processors.email.aggregators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cloud.notifications.db.EmailAggregationResources;
import com.redhat.cloud.notifications.models.AggregationCommand;
import com.redhat.cloud.notifications.models.EmailSubscriptionType;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@ApplicationScoped
class DailyEmailAggregationJob {

    private static final Logger LOG = Logger.getLogger(DailyEmailAggregationJob.class.getName());

    @Inject
    EmailAggregationResources emailAggregationResources;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Channel("aggregation")
    Emitter<String> emitter;

    @Scheduled(identity = "dailyEmailProcessor", cron = "{notifications.aggregator.email.subscription.periodic.cron}")
    public void processDailyEmail(ScheduledExecution se) throws JsonProcessingException {
        if (isCronJobEnabled()) {
            List<AggregationCommand> aggregationCommands = processAggregateEmails(se.getScheduledFireTime());
            for (AggregationCommand aggregationCommand: aggregationCommands) {
                emitter.send(objectMapper.writeValueAsString(aggregationCommand));
            }
        }
    }

    private boolean isCronJobEnabled() {
        // The scheduled job is disabled by default.
        return ConfigProvider.getConfig().getOptionalValue("notifications.aggregator.email.subscription.periodic.cron.enabled", Boolean.class).orElse(false);
    }

    List<AggregationCommand> processAggregateEmails(Instant scheduledFireTime) {
        Instant yesterdayScheduledFireTime = scheduledFireTime.minus(EmailSubscriptionType.DAILY.getDuration());

        LocalDateTime endTime = LocalDateTime.ofInstant(scheduledFireTime, UTC);
        LocalDateTime startTime = LocalDateTime.ofInstant(yesterdayScheduledFireTime, UTC);
        final LocalDateTime aggregateStarted = LocalDateTime.now();

        LOG.info(String.format("Collecting email aggregation for period (%s, %s) and type %s", startTime, endTime, EmailSubscriptionType.DAILY));

        final List<AggregationCommand> pendingAggregationCommands =
                emailAggregationResources.getApplicationsWithPendingAggregation(startTime, endTime)
                .stream()
                .map(aggregationKey -> new AggregationCommand(aggregationKey, startTime, endTime, EmailSubscriptionType.DAILY))
                .collect(Collectors.toList());

        LOG.info(
                String.format(
                        "Finished collecting email aggregations for period (%s, %s) and type %s after %d seconds. %d (accountIds, applications) pairs were processed",
                        startTime,
                        endTime,
                        EmailSubscriptionType.DAILY,
                        ChronoUnit.SECONDS.between(aggregateStarted, LocalDateTime.now()),
                        pendingAggregationCommands.size()
                )
        );

        return pendingAggregationCommands;
    }
}
