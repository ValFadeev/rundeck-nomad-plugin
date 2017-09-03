package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.List;

import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.javasdk.Predicate;


public abstract class NomadAllocationPredicates {

    private static final String CLIENT_STATUS_COMPLETE = "complete";
    private static final String CLIENT_STATUS_FAILED = "failed";

    /**
     * Returns a predicate that is true when either of the given predicates is true.
     */
    public static <T> Predicate<T> either(final Predicate<? super T> a, final Predicate<? super T> b) {
        return value -> a.apply(value) || b.apply(value);
    }

    public static Predicate<AllocationListStub> allocationHasClientStatus(final String status) {
        return allocationListStub -> status.equals(allocationListStub.getClientStatus());
    }

    public static Predicate<AllocationListStub> allocationHasCompleted() {
        return allocationHasClientStatus(CLIENT_STATUS_COMPLETE);
    }

    public static Predicate<AllocationListStub> allocationHasFailed() {
        return allocationHasClientStatus(CLIENT_STATUS_FAILED);
    }

    public static Predicate<AllocationListStub> allocationFinishedRunning() {
        return either(allocationHasCompleted(), allocationHasFailed());
    }

    public static Predicate<List<AllocationListStub>> allAllocationsFinished() {
        return allocs -> allocs.stream().allMatch(a -> allocationFinishedRunning().apply(a));
    }

    public static Predicate<List<AllocationListStub>> failedAllocationsOver(Long threshold) {
        return allocs -> {
            long failed = allocs
                    .stream()
                    .filter(a -> allocationHasFailed().apply(a))
                    .count();
            long total = allocs.size();
            long failPct = (long) ((float) failed / total * 100);
            return failPct > threshold;
        };

    }
}

