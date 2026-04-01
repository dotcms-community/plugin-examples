package com.dotcms.staticpublish.listener;

import com.dotcms.staticpublish.util.EventUtil;
import com.dotcms.system.event.local.model.EventSubscriber;
import com.dotcms.system.event.local.type.staticpublish.SingleStaticPublishEndpointSuccessEvent;
import com.dotmarketing.util.Logger;

public class SuccessEndpointsSubscriber implements EventSubscriber<SingleStaticPublishEndpointSuccessEvent> {

    public void notify(final SingleStaticPublishEndpointSuccessEvent event) {

        Logger.info(this, "Static publish endpoint success event received");
        Logger.info(this, "Config Id: " + event.getConfig().getId());
        Logger.info(this, "Endpoint: " + event.getEndpoint().getServerName());

        EventUtil.logBasicEvent(event, this.getClass());

        // TODO: implement your static publish logic here
        // e.g. copy files to CDN, FTP, S3, etc.
    }

} //SuccessEndpointsSubscriber.
