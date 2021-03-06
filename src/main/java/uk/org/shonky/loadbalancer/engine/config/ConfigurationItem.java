package uk.org.shonky.loadbalancer.engine.config;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfigurationItem {
    private String tag;
    private String field;
    private String description;
    private String type;
    private String validationURL;
    private boolean list;

    public ConfigurationItem(String tag, String field, String description, String type, String validationURL,
                             boolean list)
    {
        this.tag = checkNotNull(tag);
        this.field = checkNotNull(field);
        this.description = checkNotNull(description);
        this.type = checkNotNull(type);
        this.validationURL = checkNotNull(validationURL);
        this.list = list;
    }

    public String getTag() {
        return tag;
    }

    public String getField() {
        return field;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getValidationURL() {
        return validationURL;
    }

    public boolean isList() {
        return list;
    }
}
