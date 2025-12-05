package vn.hbtplus.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Data

public class MenuDto implements Serializable {
    private Long menuId;
    private Long parentId;
    private String menuUri;
    private String menuCode;
    private String menuName;
    private String icon;
    private String isMenu;
    @JsonIgnore
    private String scope;

    private List<String> scopes = new ArrayList<>();
    private List<MenuDto> children;

    public void addChild(MenuDto item) {
        if(children == null){
            children = new ArrayList<>();
        }
        children.add(item);
    }
}
