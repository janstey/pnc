package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildDriver;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class WaitBuildToCompleteHandler extends OperationHandlerBase implements OperationHandler {

    private final BuildDriverFactory buildDriverFactory;

    @Inject
    public WaitBuildToCompleteHandler(BuildDriverFactory buildDriverFactory) {
        this.buildDriverFactory = buildDriverFactory;
    }

    @Override
    protected TaskStatus.Operation executeAfter() {
        return TaskStatus.Operation.BUILD_SCHEDULED;
    }

    @Override
    protected void doHandle(BuildTask buildTask) {
        buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.WAITING_BUILD_TO_COMPLETE, TaskStatus.State.STARTED));
        try {
            Consumer<String> onComplete = (jobId) -> {
                buildTask.onStatusUpdate(new TaskStatus(TaskStatus.Operation.WAITING_BUILD_TO_COMPLETE, TaskStatus.State.COMPLETED));
            };

            Consumer<Exception> onError = (e) -> {
                buildTask.onError(e);
            };

            //TODO better validation
            assert (buildTask.getBuildDetails() != null);

            BuildDriver buildDriver = buildDriverFactory.getBuildDriver(buildTask.getProjectBuildConfiguration().getEnvironment().getBuildType());
            buildDriver.waitBuildToComplete(buildTask.getBuildDetails(), onComplete, onError);

        } catch (CoreException e) {
            buildTask.onError(e);
        }
    }
}
