package com.dotcms.plugin.aop.rest;

import com.dotcms.business.CloseDBIfOpened;
import com.dotcms.business.WrapInTransaction;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.contentlet.struts.ContentletForm;
import com.dotmarketing.portlets.htmlpageasset.business.HTMLPageAssetAPI;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import java.util.Calendar;

public class MyContentService {

    private final IdentifierAPI identifierAPI;
    private final MyESContentFactoryImpl contentFactory;

    public MyContentService() {
        this(  APILocator.getIdentifierAPI(), new MyESContentFactoryImpl() );
    }

    protected MyContentService(final IdentifierAPI identifierAPI,
                               final MyESContentFactoryImpl contentFactory) {

        this.identifierAPI  = identifierAPI;
        this.contentFactory = contentFactory;
    }

    @CloseDBIfOpened
    public Contentlet hydrateContentLet (final Contentlet contentlet) {

        Contentlet newContentlet = contentlet;
        if (null != contentlet && !contentlet.getMap().containsKey(HTMLPageAssetAPI.URL_FIELD)) {

            final String url = this.getUrl(contentlet.getMap().get( ContentletForm.IDENTIFIER_KEY ));
            if (null != url) {
                // making a copy to avoid issues on modifying cache objects.
                newContentlet = new Contentlet();
                newContentlet.getMap().putAll(contentlet.getMap());
                newContentlet.getMap().put(HTMLPageAssetAPI.URL_FIELD, url);
            }
        }

        return newContentlet;
    } // hydrateContentLet.

    private String getUrl ( final Object identifierObj) {

        String url = null;
        if ( identifierObj != null ) {
            try {

                final Identifier identifier = this.identifierAPI.find(  (String) identifierObj );
                url = ( UtilMethods.isSet( identifier ) && UtilMethods.isSet( identifier.getId() ) )?
                        identifier.getAssetName():null;
            } catch ( DotDataException e ) {
                Logger.error( this.getClass(), "Unable to get Identifier with id [" + identifierObj + "]. Could not get the url", e );
            }
        }

        return url;
    } // getUrl.

    @WrapInTransaction
    public int deleteLastMonthContent() throws DotDataException {

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, -1);
        return this.contentFactory.deleteContentFrom(calendar.getTime());
    }
}
