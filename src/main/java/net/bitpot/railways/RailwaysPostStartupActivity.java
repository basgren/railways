package net.bitpot.railways;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.bitpot.railways.routesView.RoutesView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class RailwaysPostStartupActivity implements ProjectActivity {
    private Project myProject;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        myProject = project;
        myProject.getMessageBus()
                .connect()
                .subscribe(ModuleListener.TOPIC, new ProjectModulesListener());

        return Unit.INSTANCE;
    }


    private class ProjectModulesListener implements ModuleListener {

        @Override
        public void modulesAdded(@NotNull Project project, @NotNull List<? extends Module> modules) {
            if (project != myProject) {
                return;
            }

            RoutesView routesView = RoutesView.getInstance(project);

            for (Module module : modules) {
                routesView.addModulePane(module);
            }
        }

        @Override
        public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
            if (project == myProject) {
                RoutesView routesView = RoutesView.getInstance(project);
                routesView.removeModulePane(module);
            }
        }
    }
}
