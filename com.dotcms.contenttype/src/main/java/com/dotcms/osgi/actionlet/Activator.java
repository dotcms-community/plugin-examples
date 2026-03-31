package com.dotmarketing.osgi.actionlet;

import com.dotcms.contenttype.model.field.DataTypes;
import com.dotcms.contenttype.model.field.Field;
import com.dotcms.contenttype.model.field.ImmutableBinaryField;
import com.dotcms.contenttype.model.field.ImmutableTextField;
import com.dotcms.contenttype.model.type.ContentType;
import com.dotcms.contenttype.model.type.ImmutableSimpleContentType;
import com.dotcms.contenttype.model.type.SimpleContentType;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.workflows.business.WorkflowAPI;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Activator extends GenericBundleActivator {

    private static final String VARIABLE_NAME = "MyContentType";

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        //Initializing services...
        initializeServices( bundleContext );

        //Registering the test Actionlet
        registerContentType();
    }

    private void registerContentType() throws DotDataException, DotSecurityException {

        final SimpleContentType simpleContentType = ImmutableSimpleContentType.builder()
                .name("My Content Type")
                .variable(VARIABLE_NAME)
                .build();

        final List<Field> newFields = new ArrayList<>();
        final ImmutableBinaryField screenshotField = ImmutableBinaryField.builder().name("Screenshot").variable("screenshot").build();
        final ImmutableTextField   titleField      = ImmutableTextField.builder().name("title").variable("title").build();
        final ImmutableTextField   urlField        = ImmutableTextField.builder().name("url").variable("url").required(true).indexed(true).unique(false).build();
        final ImmutableTextField   orderField      = ImmutableTextField.builder().name("order").dataType(DataTypes.INTEGER).variable("order").indexed(true).build();

        newFields.add(screenshotField);
        newFields.add(titleField);
        newFields.add(urlField);
        newFields.add(orderField);
        final ContentType savedContentType = APILocator.getContentTypeAPI(APILocator.systemUser())
                .save(simpleContentType, newFields, null); // saves the content type with the new fields

        // rel the system workflow with the content type
        final Set<String> workflowIds = new HashSet<>();
        workflowIds.add(WorkflowAPI.SYSTEM_WORKFLOW_ID);

        APILocator.getWorkflowAPI().saveSchemeIdsForContentType(savedContentType, workflowIds);
    }

    public void stop(BundleContext context) throws Exception {

        // Remove the content type
        final ContentType contentType = APILocator.getContentTypeAPI(APILocator.systemUser()).find(VARIABLE_NAME);
        if (null != contentType) {
            APILocator.getContentTypeAPI(APILocator.systemUser()).delete(contentType);
        }
                //Unregister all the bundle services
        unregisterServices(context);
    }

}
