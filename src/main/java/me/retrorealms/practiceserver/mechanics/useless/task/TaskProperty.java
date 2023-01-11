package me.retrorealms.practiceserver.mechanics.useless.task;

/**
 * Created by Giovanni on 8-7-2017.
 */
public class TaskProperty<O, T, C> {

    private O propertyDefault;
    private T propertyIndex;
    private C propertyOption;

    public TaskProperty(O propertyDefault, T propertyIndex, C propertyOption) {
        this.propertyDefault = propertyDefault;
        this.propertyIndex = propertyIndex;
        this.propertyOption = propertyOption;
    }

    public O getPropertyDefault() {
        return propertyDefault;
    }

    public T getPropertyIndex() {
        return propertyIndex;
    }

    public C getPropertyOption() {
        return propertyOption;
    }
}
