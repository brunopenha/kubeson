package br.nom.penha.bruno.kubeson.common.util;

import br.nom.penha.bruno.kubeson.common.model.SelectorItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ComboBoxItemWrap {

    private BooleanProperty check = new SimpleBooleanProperty(false);
    private ObjectProperty<SelectorItem> item = new SimpleObjectProperty<>();

    ComboBoxItemWrap() {
    }

    ComboBoxItemWrap(SelectorItem item) {
        this.item.set(item);
    }

    ComboBoxItemWrap(SelectorItem item, Boolean check) {
        this.item.set(item);
        this.check.set(check);
    }

    public BooleanProperty checkProperty() {
        return check;
    }

    public Boolean getCheck() {
        return check.getValue();
    }

    public void setCheck(Boolean value) {
        check.set(value);
    }

    public ObjectProperty<SelectorItem> itemProperty() {
        return item;
    }

    public SelectorItem getItem() {
        return item.getValue();
    }

    public void setItem(SelectorItem value) {
        item.setValue(value);
    }

    @Override
    public String toString() {
        return item.getValue().toString();
    }
}
