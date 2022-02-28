package br.nom.penha.bruno.kubeson.common.model;

import br.nom.penha.bruno.kubeson.logs.model.LogLine;
import br.nom.penha.bruno.kubeson.logs.model.LogSource;

public interface PodLogFeedListener {

    void onNewLogLine(LogSource logSource, LogLine logLine);

    void onLogLineRemoved(LogLine logLine);

    void onPodLogFeedTerminated(K8SPod pod);

}
