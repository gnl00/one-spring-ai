package one.demo.functioncallback;

import lombok.Getter;

@Getter
public enum Unit {

    C("metric"),
    F("imperial");

    private final String unitName;

    Unit(String unitName) {
        this.unitName = unitName;
    }

}
