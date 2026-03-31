package com.dotmarketing.osgi.hooks;

import com.dotcms.dotpubsub.DotPubSubEvent;
import com.dotcms.dotpubsub.DotPubSubProvider;
import com.dotcms.dotpubsub.DotPubSubProviderLocator;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPostHookAbstractImp;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;
import com.liferay.portal.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This hook sends an event topic when content is being publish
 * @author jsanca
 */
public class PublishContentPostContentHook extends ContentletAPIPostHookAbstractImp {

    private final static  String TOPIC_NAME = OnPublishContentTopic.PUBLISH_CONTENT_TOPIC;
    private final DotPubSubProvider     pubsub;
    private final OnPublishContentTopic onPublishContentTopic;

    public PublishContentPostContentHook() {
        super();
        this.pubsub                = DotPubSubProviderLocator.provider.get();
        this.onPublishContentTopic = new OnPublishContentTopic();
        Logger.debug(this.getClass(), "Starting hook with PubSub");

        this.pubsub.start();
        this.pubsub.subscribe(this.onPublishContentTopic);
    }

    @Override
    public void publish(final Contentlet contentlet, final User user, final boolean respectFrontendRoles) {

        Logger.debug(this, ()-> "Contentlet: " + contentlet.getIdentifier() + " has been published");

        final DotPubSubEvent event = new DotPubSubEvent.Builder ()
                .addPayload("contentlets", new ArrayList<>(Arrays.asList(contentlet.getIdentifier())))
                .withTopic(TOPIC_NAME)
                .withType(OnPublishContentTopic.EventType.PUBLISH_CONTENT_REQUEST.name())
                .build();

        this.pubsub.publish(event);
    }

    @Override
    public void publish(final List<Contentlet> contentlets, final User user, final boolean respectFrontendRoles) {

        Logger.debug(this, ()-> "Contentlet: " +
                contentlets.stream().map(c -> c.getIdentifier()).collect(Collectors.joining())
                + " has been published");
        final DotPubSubEvent event = new DotPubSubEvent.Builder ()
                .addPayload("contentlets", new ArrayList<>(contentlets.stream().collect(Collectors.toList())))
                .withTopic(TOPIC_NAME)
                .withType(OnPublishContentTopic.EventType.PUBLISH_CONTENT_REQUEST.name())
                .build();

        this.pubsub.publish(event);
    }
}
