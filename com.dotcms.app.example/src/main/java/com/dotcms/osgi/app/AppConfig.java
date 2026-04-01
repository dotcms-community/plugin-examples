package com.dotcms.osgi.app;

import java.io.Serializable;
import java.util.Arrays;
import com.dotmarketing.util.UtilMethods;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = AppConfig.Builder.class)
public class AppConfig implements Serializable {


    private static final long serialVersionUID = 1L;

    public AppConfig() {
        this.checkbox = false;
        this.variableOne = null;
        this.variableTwo = null;
        this.selectVariable = null;
    }


    public final boolean checkbox;
    public final String variableOne;
    public final char[] variableTwo;
    public final String selectVariable;


    
    @Override
    public String toString() {
        return "AppConfig {checkbox:" + checkbox + ", variableOne:" + variableOne + ", variableTwo char[]:" + variableTwo
                        + ", selectVariable:" + selectVariable + "}";
    }


    private AppConfig(Builder builder) {
        this.checkbox = builder.checkbox;
        this.variableOne = builder.variableOne;
        this.variableTwo = builder.variableTwo;
        this.selectVariable = builder.selectVariable;


    }


    /**
     * Creates builder to build {@link AnalyticsConfig}.
     * 
     * @return created builder
     */

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder to build {@link AnalyticsConfig} and initialize it with the given object.
     * 
     * @param analyticsConfig to initialize the builder with
     * @return created builder
     */

    public static Builder from(AppConfig appConfig) {
        return new Builder(appConfig);
    }

    /**
     * Builder to build {@link AnalyticsConfig}.
     */

    public static final class Builder {
        private boolean checkbox;
        private String variableOne,  selectVariable;
        private char[] variableTwo;

        private Builder() {}

        private Builder(AppConfig appConfig) {
            this.checkbox = appConfig.checkbox;
            this.variableOne = appConfig.variableOne;
            this.variableTwo = appConfig.variableTwo;
            this.selectVariable = appConfig.selectVariable;


        }

        public Builder checkbox(boolean checkbox) {
            this.checkbox = checkbox;
            return this;
        }


        public Builder variableOne(String variableOne) {
            this.variableOne = variableOne;
            return this;
        }

        public Builder variableTwo(char[] variableTwo) {
            this.variableTwo = variableTwo;
            return this;
        }

        public Builder selectVariable(String selectVariable) {
            this.selectVariable = selectVariable;
            return this;
        }


        public AppConfig build() {
            return new AppConfig(this);
        }

        final String[] strToArray(final String val) {

            return UtilMethods.isSet(val)
                            ? Arrays.stream(val.toLowerCase().split(",")).map(String::trim).toArray(String[]::new)
                            : new String[0];

        }

    }


}
