package jobs;

import java.io.File;

import play.Logger;
import play.jobs.Job;

public class FileCleanupJob extends Job {

    private static final int CLEANUP_DELAY_IN_SECONDS = 60; // 1 minute

    public static void scheduleCleanup(File file) {
        new FileCleanupJob(file).in(CLEANUP_DELAY_IN_SECONDS);
    }

    private final File file;

    private FileCleanupJob(File file) {
        this.file = file;
    }

    @Override
    public void doJob() throws Exception {
        Logger.info("Deleting temp file %s", file.getAbsolutePath());
        file.delete();
    }

}
