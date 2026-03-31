package com.dotmarketing.osgi.hooks;

import com.dotcms.contenttype.model.type.ContentType;
import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.ContentletAPIPreHook;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;


/**
 * This hook does the validation for diff content types
 * @author jsanca
 */
public class ValidatorPreContentHook implements ContentletAPIPreHook {

    public final Set<ValidatorStrategy> validatorSet = new LinkedHashSet<>();

    /**
     * Adds a validators
     * @param validators
     */
    public void addValidator (final ValidatorStrategy... validators) {

        this.validatorSet.addAll(Arrays.asList(validators));
    }


    public ValidatorPreContentHook() {
        super();
    }


	@Override
    public boolean validateContentlet(final Contentlet contentlet, final ContentletRelationships contentRelationships,
                                   final List<Category> categories) throws DotContentletValidationException {

        for (final ValidatorStrategy validator : this.validatorSet) {

            if (validator.test(contentlet, contentRelationships, categories)) {

                validator.applyValidation(contentlet, contentRelationships, categories);
            }
        }

        return true;
    }
}