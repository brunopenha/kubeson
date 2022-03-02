module br.nom.penha.bruno {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.apache.logging.log4j;
    requires org.apache.commons.lang3;
    requires flowless;
    requires kubernetes.client;
    requires kubernetes.model.core;
    requires com.fasterxml.jackson.databind;
    requires proxy.vole;
    requires controlsfx;
    requires okhttp3;
    exports br.nom.penha.bruno;
    exports br.nom.penha.bruno.controller;
    opens br.nom.penha.bruno.controller to javafx.fxml;
    opens br.nom.penha.bruno.kubeson to javafx.graphics;
}