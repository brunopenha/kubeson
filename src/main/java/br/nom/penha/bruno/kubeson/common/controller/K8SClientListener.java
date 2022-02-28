package br.nom.penha.bruno.kubeson.common.controller;

import br.nom.penha.bruno.kubeson.common.model.K8SConfigMap;
import br.nom.penha.bruno.kubeson.common.model.K8SPod;

public interface K8SClientListener {

    void onPodChange(K8SResourceChange<K8SPod> changes);

    void onConfigMapChange(K8SResourceChange<K8SConfigMap> changes);
}
