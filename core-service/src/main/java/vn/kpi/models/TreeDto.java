package vn.kpi.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class TreeDto {
    private String nodeId;
    private String name;
    private String code;
    private String parentId;
    private String pathId;
    private Integer totalChildren;
    private List<TreeDto> children;

    public void addChild(TreeDto item) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(item);
    }
}
