package com.dotmarketing.osgi.hooks;

import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;

import java.util.List;

/**
 * Validator Strategy for contentlets
 * First the hook will see if the validator applies for the current contentlet info, if does will apply the validation
 *
 * @author jsanca
 */
public interface ValidatorStrategy {

    /**
     * Returns true if the validator strategy applies, for instance if the contentlet is some type or so
     * @return boolean
     */
    boolean test(final Contentlet contentlet, final ContentletRelationships contentRelationships,
                 final List<Category> categories);

    /**
     * Applies the validation, throws an DotContentletValidationException if has an error
     * @param contentlet
     * @param contentRelationships
     * @param categories
     * @return boolean true if everything ok
     * @throws DotContentletValidationException
     */
    boolean applyValidation(final Contentlet contentlet, final ContentletRelationships contentRelationships,
                            final List<Category> categories) throws DotContentletValidationException;
}
