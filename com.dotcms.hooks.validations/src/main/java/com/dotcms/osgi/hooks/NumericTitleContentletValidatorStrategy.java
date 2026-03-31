package com.dotmarketing.osgi.hooks;

import com.dotmarketing.portlets.categories.model.Category;
import com.dotmarketing.portlets.contentlet.business.DotContentletValidationException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.structure.model.ContentletRelationships;
import com.dotmarketing.util.UtilMethods;

import java.util.List;

/**
 * This validation for the Generic Contentlet
 * for this example the title must start with a number
 * @author jsanca
 */
public class NumericTitleContentletValidatorStrategy implements ValidatorStrategy {

    /**
     * This method will be called in order to see if the {@link ValidatorStrategy} can be apply to the arguments
     * in our case if the contentlet type is a content generic, applies
     * @param contentlet
     * @param contentRelationships
     * @param categories
     * @return boolean returns true if the strategy can be apply
     */
    @Override
    public boolean test(final Contentlet contentlet,
                        final ContentletRelationships contentRelationships,
                        final List<Category> categories) {

        final String variable = contentlet.getContentType().variable();
        return "webPageContent".equals(variable);
    }

    @Override
    public boolean applyValidation(final Contentlet contentlet,
                                   final ContentletRelationships contentRelationships,
                                   final List<Category> categories) throws DotContentletValidationException {

        final String title = contentlet.getTitle();
        if (!UtilMethods.isSet(title)) {

            throw new DotContentletValidationException("The content must starts with a number");
        }

        final char firstLetter = title.charAt(0);
        if (!Character.isDigit(firstLetter)) {

            throw new DotContentletValidationException("The content must starts with a number");
        }

        return true;
    }
}
