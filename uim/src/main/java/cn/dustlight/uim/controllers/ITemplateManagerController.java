package cn.dustlight.uim.controllers;

import cn.dustlight.uim.RestfulResult;
import org.apache.ibatis.annotations.Delete;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/api/template")
public interface ITemplateManagerController {

    @GetMapping("/names")
    @PreAuthorize("hasAnyRole('ROOT','ADMIN')")
    RestfulResult<List<String>> getTemplateNames() throws IOException;

    @GetMapping("/text/{name}")
    @PreAuthorize("hasAnyRole('ROOT','ADMIN')")
    RestfulResult<String> getTemplate(@PathVariable String name) throws IOException;

    @PostMapping("/text/{name}")
    @PreAuthorize("hasAnyRole('ROOT','ADMIN')")
    RestfulResult setTemplate(@PathVariable String name, @RequestParam String text) throws IOException;

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ROOT','ADMIN')")
    RestfulResult deleteTemplate(@RequestBody String[] names) throws IOException;
}
