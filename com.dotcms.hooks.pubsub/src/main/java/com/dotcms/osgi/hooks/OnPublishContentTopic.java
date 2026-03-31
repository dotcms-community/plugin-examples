package com.dotmarketing.osgi.hooks;

import com.dotcms.dotpubsub.DotPubSubEvent;
import com.dotcms.dotpubsub.DotPubSubProvider;
import com.dotcms.dotpubsub.DotPubSubProviderLocator;
import com.dotcms.dotpubsub.DotPubSubTopic;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.StringUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This implementation handles the pub/sub when a content is being publish
 * In this way cluster nodes might be aware about the publish on any node
 * @author jsanca
 */
public class OnPublishContentTopic implements DotPubSubTopic {

    public static final String PUBLISH_CONTENT_TOPIC     = "dotpublishcontent_topic";

    private static long bytesSent, bytesReceived, messagesSent, messagesReceived = 0;

    @VisibleForTesting
    private final String serverId;

    private final DotPubSubProvider provider;

    final Map<String, Consumer<DotPubSubEvent>> consumerMap =
            new ImmutableMap.Builder<String, Consumer<DotPubSubEvent>>()
                    .put(EventType.PUBLISH_CONTENT_REQUEST.name(),  (event) -> {

                        Logger.info(this.getClass(), () -> "Got PUBLISH_CONTENT_REQUEST from server:" + event.getOrigin() + ". sending response");

                        OnPublishContentTopic.this.provider.publish(new DotPubSubEvent.Builder(event)
                                // we set response b/c the nodes subscribed will wait this kind of msg
                                .withType(EventType.PUBLISH_CONTENT_RESPONSE.name()).withTopic(OnPublishContentTopic.this)
                                .build());
                    })
                    .put(EventType.PUBLISH_CONTENT_RESPONSE.name(), (event) -> {

                        Logger.info(this.getClass(),
                                () -> "Got PUBLISH_CONTENT_RESPONSE from server:" + event.getOrigin() + ". Saving response");

                        final String origin = (String) event.getPayload().get("serverId");
                        Logger.info(this, "Event received: " + event.toString());
                    }).build();

    /**
     * Events types to handle
     * request: made on the content api
     * response: handle by the Pub/Sub
     * unknown
     */
    public enum EventType {

        PUBLISH_CONTENT_REQUEST, PUBLISH_CONTENT_RESPONSE, UNKNOWN;

        private static final EventType [] types = values();

        public static  EventType from(final Serializable name) {

            for (final EventType type : types) {
                if (type.name().equals(name)) {
                    return type;
                }
            }

            return UNKNOWN;
        }
    }

    public OnPublishContentTopic() {
        this(APILocator.getServerAPI().readServerId());
    }

    @VisibleForTesting
    public OnPublishContentTopic(final String serverId) {
        this(serverId, DotPubSubProviderLocator.provider.get());
    }

    @VisibleForTesting
    public OnPublishContentTopic(final String serverId, final DotPubSubProvider provider) {
        this.serverId = StringUtils.shortify(serverId, 10);
        this.provider = provider;
    }

    public String getInstanceId () {
        return serverId;
    }

    @Override
    public Comparable getKey() {
        return PUBLISH_CONTENT_TOPIC;
    }

    @Override
    public long messagesSent() {
        return messagesSent;
    }

    @Override
    public long bytesSent() {
        return bytesSent;
    }

    @Override
    public long messagesReceived() {
        return messagesReceived;
    }

    @Override
    public long bytesReceived() {
        return bytesReceived;
    }

    @Override
    public void incrementSentCounters(final DotPubSubEvent event) {
        bytesSent += event.toString().getBytes().length;
        messagesSent++;
    }

    @Override
    public void incrementReceivedCounters(final DotPubSubEvent event) {
        bytesReceived += event.toString().getBytes().length;
        messagesReceived++;
    }

    @Override
    public void notify(final DotPubSubEvent event) {

        Logger.info(this.getClass(), () -> "Got CLUSTER_REQ from server:" + event.getOrigin() + ". sending response");

        this.consumerMap.getOrDefault(event.getType(), doNothing).accept(event);
    }

    final private Consumer<DotPubSubEvent> doNothing = (event) -> {
        Logger.debug(getClass(), () -> "got an non-event:" + event);
    };

}
