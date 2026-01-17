package net.bitpot.railways.navigation;

import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.RequestMethods;
import net.bitpot.railways.models.requestMethods.RequestMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 *
 */
public class ChooseByRouteNameFilter extends ChooseByNameFilter<RequestMethod> {
    /**
     * A constructor
     *
     * @param popup               a parent popup
     * @param model               a model for popup
     * @param filterConfiguration storage for selected filter values
     * @param project             a context project
     */
    public ChooseByRouteNameFilter(@NotNull ChooseByNamePopup popup,
                                   @NotNull FilteringGotoByModel<RequestMethod> model,
                                   @NotNull ChooseByNameFilterConfiguration<RequestMethod> filterConfiguration,
                                   @NotNull Project project) {
        super(popup, model, filterConfiguration, project);
    }


    @Override
    protected String textForFilterValue(@NotNull RequestMethod value) {
        return value.getName();
    }


    @Nullable
    @Override
    protected Icon iconForFilterValue(@NotNull RequestMethod value) {
        return value.getIcon();
    }


    @NotNull
    @Override
    protected Collection<RequestMethod> getAllFilterValues() {
        return RequestMethods.getAllRequestMethods();
    }
}