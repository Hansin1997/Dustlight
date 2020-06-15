package cn.dustlight.uim.services;

import cn.dustlight.uim.models.TemplateNode;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Mapper
public interface TemplateManagerMapper {

    @Select("SELECT name FROM sender_templates")
    List<String> getTemplatesName();

    @Select("SELECT text FROM sender_templates where name=#{templateName}")
    String getTemplate(String templateName);

    @Insert("INSERT INTO sender_templates (name,text) VALUES (#{templateName},#{templateContent}) ON DUPLICATE KEY UPDATE text=#{templateContent}")
    void setTemplate(String templateName, String templateContent);

    @Select("SELECT * FROM sender_templates")
    List<TemplateNode> getTemplates();

    @Insert({"<script><foreach collection='templates' item='template' separator=';'>",
            "INSERT INTO sender_templates (name,text) VALUES (#{template.name},#{template.text}) ON DUPLICATE KEY UPDATE text=#{template.text}",
            "</foreach></script>"})
    void setTemplates(@Param("templates") List<TemplateNode> templates);

    @Delete({"<script>DELETE FROM sender_templates WHERE name IN " +
            "<foreach collection='templateNames' item='name' open='(' separator=',' close=')'>",
            "#{name}",
            "</foreach></script>"})
    void deleteTemplate(@Param("templateNames") List<String> templateNames);
}
