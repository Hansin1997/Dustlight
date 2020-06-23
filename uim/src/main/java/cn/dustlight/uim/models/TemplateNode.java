package cn.dustlight.uim.models;

import java.io.Serializable;

public class TemplateNode implements Serializable {

    public Long id;
    public String name;
    public String text;

    public TemplateNode() {

    }

    public TemplateNode(String name, String text) {
        setName(name);
        setText(text);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    @Override
    public String toString() {
        return "TemplateNode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
