package br.nom.penha.bruno.kubeson.common.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.nom.penha.bruno.kubeson.Configuration;
import br.nom.penha.bruno.kubeson.Main;
import br.nom.penha.bruno.kubeson.common.controller.K8SApiException;
import br.nom.penha.bruno.kubeson.common.controller.K8SClient;
import br.nom.penha.bruno.kubeson.common.controller.K8SRequestCallback;
import br.nom.penha.bruno.kubeson.common.controller.K8SRequestResult;
import br.nom.penha.bruno.kubeson.common.util.CircularArrayList;
import br.nom.penha.bruno.kubeson.common.util.ThreadFactory;
import br.nom.penha.bruno.kubeson.logs.model.LogLine;
import br.nom.penha.bruno.kubeson.logs.model.LogSource;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class K8SPod {

    public static final String STATUS_PENDING = "Pending";

    public static final String STATUS_RUNNING = "Running";

    public static final String STATUS_SUCCEEDED = "Succeeded";

    public static final String STATUS_FAILED = "Failed";

    public static final String STATUS_UNKNOWN = "Unknown";

    private static final Logger LOGGER = LogManager.getLogger();

    private Pod pod;

    private String appLabel;

    private long flag;

    private List<String> containers;

    private List<String> initContainers;

    private Map<String, PodContainerThread> podThreads;

    private int metricsNodePort;

    public K8SPod(Pod pod, long flag) {
        this.pod = pod;
        this.flag = flag;
        this.podThreads = Collections.synchronizedMap(new HashMap<>());
        this.containers = new ArrayList<>();
        this.initContainers = new ArrayList<>();
        this.appLabel = getAppLabel(getLabels());

        if (pod.getStatus().getContainerStatuses() != null && pod.getStatus().getContainerStatuses().size() > 1) {
            for (ContainerStatus containerStatus : pod.getStatus().getContainerStatuses()) {
                containers.add(containerStatus.getName());
            }
        }

        if (pod.getStatus().getInitContainerStatuses() != null) {
            for (ContainerStatus containerStatus : pod.getStatus().getInitContainerStatuses()) {
                initContainers.add(containerStatus.getName());
            }
        }
    }

    public boolean usesConfigMap(String configMapName) {
        for (Volume volume : pod.getSpec().getVolumes()) {
            if (volume.getConfigMap() != null && volume.getConfigMap().getName().equals(configMapName)) {
                return true;
            }
        }

        return false;
    }

    public static String getAppLabel(Map<String, String> labels) {
        if (labels != null) {
            for (String appLabel : Configuration.KUBERNETES_APP_LABELS) {
                final String labelValue = labels.get(appLabel);
                if (!StringUtils.isEmpty(labelValue)) {
                    return labelValue;
                }
            }
        }

        return null;
    }

    public long getFlag() {
        return flag;
    }

    public void setFlag(long flag) {
        this.flag = flag;
    }

    public String getUid() {
        return pod.getMetadata().getUid();
    }

    public String getNamespace() {
        return pod.getMetadata().getNamespace();
    }

    public String getPodName() {
        return pod.getMetadata().getName();
    }

    public Map<String, String> getLabels() {
        if (pod.getMetadata().getLabels() == null) {
            return new HashMap<>();
        }
        return pod.getMetadata().getLabels();
    }

    public boolean containsLabel(String labelName, String labelValue) {
        String value = getLabels().get(labelName);

        return value != null && value.equals(labelValue);
    }

    public String getAppLabel() {
        return appLabel;
    }

    public String getState() {
        return pod.getStatus().getPhase();
    }

    public void setState(String state) {
        pod.getStatus().setPhase(state);
    }

    public Instant getStartTime() {
        return Instant.parse(pod.getStatus().getStartTime());
    }

    public List<String> getContainers() {
        return containers;
    }

    public List<String> getInitContainers() {
        return initContainers;
    }

    public boolean isContainerOnly() {
        return getContainers().size() >= 2;
    }

    public int getMetricsNodePort() {
        return metricsNodePort;
    }

    public void setMetricsNodePort(int metricsNodePort) {
        this.metricsNodePort = metricsNodePort;
    }

    public boolean hasMetrics() {
        return this.metricsNodePort > 0;
    }

    public void delete() {
        delete(null);
    }

    public void delete(K8SRequestCallback requestCallback) {
        ThreadFactory.newThread(() -> {
            try {
                List<StatusDetails> status = K8SClient.deletePod(getNamespace(),pod);
                K8SRequestResult.apply(requestCallback, true);
            } catch (K8SApiException e) {
                LOGGER.error("Failed to delete pod " + getPodName(), e);
                Platform.runLater(() -> {
                    Main.showErrorMessage("Failed to delete pod " + getPodName(), e);
                    K8SRequestResult.apply(requestCallback, false, e);
                });
            }
        });
    }

    public void addListener(String container, LogSource logSource, PodLogFeedListener podLogFeedListener, boolean showLogsFromStart) {
        if (container == null || containers.contains(container) || initContainers.contains(container)) {
            PodContainerThread podContainerThread = podThreads.get(container);
            if (podContainerThread != null) {
                if (showLogsFromStart && podContainerThread.logLines.size() > 0) {
                    podContainerThread.logLines.forEach(logLine -> podLogFeedListener.onNewLogLine(logSource, logLine));
                }
                podContainerThread.logSources.add(logSource);
                podContainerThread.podLogFeedListeners.add(podLogFeedListener);
            } else {
                podContainerThread = new PodContainerThread();
                podContainerThread.logSources.add(logSource);
                podContainerThread.podLogFeedListeners.add(podLogFeedListener);
                podContainerThread.threadFactory = ThreadFactory.newThread(createRunnable(podContainerThread, container));
                podThreads.put(container, podContainerThread);
            }
        }
    }

    public void removeListener(PodLogFeedListener podLogFeedListener, boolean keepLogSource) {
        Iterator<Map.Entry<String, PodContainerThread>> entryIt = podThreads.entrySet().iterator();

        while (entryIt.hasNext()) {
            PodContainerThread podContainerThread = entryIt.next().getValue();
            final int idx = podContainerThread.podLogFeedListeners.indexOf(podLogFeedListener);
            if (idx > -1) {
                podContainerThread.podLogFeedListeners.remove(idx);
                podContainerThread.logSources.remove(idx);

                if (!keepLogSource && podContainerThread.podLogFeedListeners.isEmpty()) {
                    podContainerThread.threadFactory.interrupt();
                    entryIt.remove();
                }
            }
        }
    }

    public boolean isRunning(String container) {
        PodContainerThread podContainerThread = podThreads.get(container);
        if (podContainerThread != null && !podContainerThread.threadFactory.isInterrupted()) {
            return true;
        }
        return false;
    }

    public void terminate() {
        podThreads.forEach((container, podContainerThread) -> podContainerThread.threadFactory.interrupt());
    }

    private Runnable createRunnable(final PodContainerThread podContainerThread, final String container) {
        return () -> {
            LOGGER.debug("Starting log stream for {} with container {}", this, container);

            LogWatch logWatch = K8SClient.getLogs(getNamespace(), getPodName(), container, Configuration.MAX_LOG_LINES * 2);
            try (InputStream is = logWatch.getOutput()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                for (; ; ) {
                    if(null != in.readLine()){
                        final String line = in.readLine();
                        final LogLine logLine = new LogLine(line);
                        if (podContainerThread.logLines.size() >= Configuration.MAX_LOG_LINES) {
                            podContainerThread.podLogFeedListeners.forEach(
                                    podLogFeedListener -> podLogFeedListener.onLogLineRemoved(podContainerThread.logLines.get(0)));
                            podContainerThread.logLines.remove(0);
                        }
                        podContainerThread.logLines.add(logLine);
                        for (int i = 0; i < podContainerThread.podLogFeedListeners.size(); i++) {
                            podContainerThread.podLogFeedListeners.get(i).onNewLogLine(podContainerThread.logSources.get(i), logLine);
                        }
                    }else{
                        break;
                    }
                }
            } catch (InterruptedIOException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.error("Log stream stopped unexpectedly for " + this, e);
            }
            logWatch.close();
            for (PodLogFeedListener podLogFeedListener : podContainerThread.podLogFeedListeners) {
                podLogFeedListener.onPodLogFeedTerminated(this);
            }
            podContainerThread.logLines.clear();
            LOGGER.debug("Log stream stopped for {}", this);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        K8SPod pod = (K8SPod) o;
        return Objects.equals(getUid(), pod.getUid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUid());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pod{");
        sb.append("uid='").append(getUid()).append('\'');
        sb.append(", namespace='").append(getNamespace()).append('\'');
        sb.append(", podName='").append(getPodName()).append('\'');
        sb.append(", labels=").append(getLabels());
        sb.append(", state='").append(getState()).append('\'');
        sb.append(", containers=").append(getContainers());
        sb.append(", initContainers=").append(getInitContainers());
        sb.append(", metricsNodePort=").append(metricsNodePort);
        sb.append('}');
        return sb.toString();
    }

    private static class PodContainerThread {

        private ThreadFactory threadFactory;

        private List<LogSource> logSources;

        private List<PodLogFeedListener> podLogFeedListeners;

        private List<LogLine> logLines;

        private PodContainerThread() {
            this.logSources = new ArrayList<>();
            this.podLogFeedListeners = new ArrayList<>();
            this.logLines = Collections.synchronizedList(new CircularArrayList<>(Configuration.MAX_LOG_LINES));
        }
    }
}
