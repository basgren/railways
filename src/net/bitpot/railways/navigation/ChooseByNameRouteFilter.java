package net.bitpot.railways.navigation;

import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.models.routes.RequestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 *
 */
public class ChooseByNameRouteFilter extends ChooseByNameFilter<RequestType> {
    /**
     * A constructor
     *
     * @param popup               a parent popup
     * @param model               a model for popup
     * @param filterConfiguration storage for selected filter values
     * @param project             a context project
     */
    public ChooseByNameRouteFilter(@NotNull ChooseByNamePopup popup,
                                   @NotNull FilteringGotoByModel<RequestType> model,
                                   @NotNull ChooseByNameFilterConfiguration<RequestType> filterConfiguration,
                                   @NotNull Project project) {
        super(popup, model, filterConfiguration, project);
    }


    @Override
    protected String textForFilterValue(@NotNull RequestType value) {
        return value.getName();
    }


    @Nullable
    @Override
    protected Icon iconForFilterValue(@NotNull RequestType value) {
        return value.getIcon();
    }


    @NotNull
    @Override
    protected Collection<RequestType> getAllFilterValues() {
        return RequestType.getAllRequestTypes();
    }
}