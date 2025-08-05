package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeStructure {
    private String nodeType;      // "dict" 或 "leaf"
    private String varType;       // "dict", "string", "int", "date" 等
    private String name;          // 字段名
    private String nameDisplay;   // 显示名称
    private List<NodeStructure> children;  // 子节点（仅当nodeType为"dict"时使用）
    
    // 创建字典节点的便捷构造函数
    public static NodeStructure createDict(String name, String nameDisplay, List<NodeStructure> children) {
        return new NodeStructure("dict", "dict", name, nameDisplay, children);
    }
    
    // 创建叶子节点的便捷构造函数
    public static NodeStructure createLeaf(String varType, String name, String nameDisplay) {
        return new NodeStructure("leaf", varType, name, nameDisplay, null);
    }
}
