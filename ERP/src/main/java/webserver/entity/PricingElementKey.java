package webserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 定价元素类型配置实体类
 */
public class PricingElementKey {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("defaultUnit")
    private String defaultUnit;
    
    @JsonProperty("rule")
    private String rule;
    
    @JsonProperty("sortKey")
    private Integer sortKey;
    
    @JsonProperty("config")
    private String config;
    
    // 默认构造函数
    public PricingElementKey() {}
    
    // 全参构造函数
    public PricingElementKey(Integer id, String name, String description, String defaultUnit, 
                           String rule, Integer sortKey, String config) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultUnit = defaultUnit;
        this.rule = rule;
        this.sortKey = sortKey;
        this.config = config;
    }
    
    // Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDefaultUnit() {
        return defaultUnit;
    }
    
    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }
    
    public String getRule() {
        return rule;
    }
    
    public void setRule(String rule) {
        this.rule = rule;
    }
    
    public Integer getSortKey() {
        return sortKey;
    }
    
    public void setSortKey(Integer sortKey) {
        this.sortKey = sortKey;
    }
    
    public String getConfig() {
        return config;
    }
    
    public void setConfig(String config) {
        this.config = config;
    }
    
    @Override
    public String toString() {
        return "PricingElementKey{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultUnit='" + defaultUnit + '\'' +
                ", rule='" + rule + '\'' +
                ", sortKey=" + sortKey +
                ", config='" + config + '\'' +
                '}';
    }
}
