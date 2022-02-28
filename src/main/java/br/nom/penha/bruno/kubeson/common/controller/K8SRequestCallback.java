package br.nom.penha.bruno.kubeson.common.controller;

@FunctionalInterface
public interface K8SRequestCallback {

    void callback(K8SRequestResult requestResult);
}
